package com.zerobase.order_drinks.service;

import com.zerobase.order_drinks.components.MailComponent;
import com.zerobase.order_drinks.exception.impl.member.*;
import com.zerobase.order_drinks.model.constants.MemberStatus;
import com.zerobase.order_drinks.model.dto.Auth;
import com.zerobase.order_drinks.model.dto.Member;
import com.zerobase.order_drinks.model.entity.MemberEntity;
import com.zerobase.order_drinks.model.entity.Wallet;
import com.zerobase.order_drinks.model.constants.EmailAuthStatus;
import com.zerobase.order_drinks.repository.CardRepository;
import com.zerobase.order_drinks.repository.CouponRepository;
import com.zerobase.order_drinks.repository.MemberRepository;
import com.zerobase.order_drinks.repository.PointRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@AllArgsConstructor
public class MemberService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final MailComponent mailComponent;
    private final CouponRepository couponRepository;
    private final CardRepository cardRepository;
    private final PointRepository pointRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("couldn`t find user -> " + username));
    }

    public MemberEntity register(Auth.SignUp member){
        boolean exists = this.memberRepository.existsByUsername(member.getUsername());
        if(exists){
            throw new AlreadyExistUserException();
        }

        boolean validEmail = isValidEmail(member.getUsername());
        if(!validEmail){
            throw new NoEmailPatternException();
        }

        member.setPassword(this.passwordEncoder.encode(member.getPassword()));

        Wallet.Card card = new Wallet.Card();
        card.setChargedDate(LocalDateTime.now());
        cardRepository.save(card);

        Wallet.Coupon coupon = new Wallet.Coupon();
        coupon.setCount(1);
        coupon.setUpdatedDate(LocalDateTime.now());
        couponRepository.save(coupon);

        Wallet.Point point = new Wallet.Point();
        point.setUpdatedDate(LocalDateTime.now());
        pointRepository.save(point);

        String uuid = UUID.randomUUID().toString();
        var result = this.memberRepository.save(member.toEntity(uuid, card, coupon, point));

        String email = member.getUsername();
        mailComponent.sendMail(email, uuid);

        return result;
    }

    private final static boolean isValidEmail(String email) {
        boolean err = false;
        String regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    public MemberEntity authenticate(Auth.SignIn member){
        var user = this.memberRepository.findByUsername(member.getUsername())
                .orElseThrow(() -> new NoUserException());

        if(!this.passwordEncoder.matches(member.getPassword(), user.getPassword())){
            throw new PasswordNotMatchException();
        }

        if(user.getEmailAuthStatus() == EmailAuthStatus.ING){
            throw new NoEmailAuthException();
        }

        if(user.getMemberStatus() == MemberStatus.WITHDRAW){
            throw new WithdrawUserException();
        }

        return user;
    }

    public Member emailAuth(String uuid){
        Optional<MemberEntity> optionalMember = memberRepository.findByEmailAuthKey(uuid);

        if(!optionalMember.isPresent()){
            throw new NoUserException();
        }

        MemberEntity memberEntity = optionalMember.get();
        memberEntity.setEmailAuthDateTime(LocalDateTime.now());
        memberEntity.setEmailAuthStatus(EmailAuthStatus.COMPLETE);
        memberRepository.save(memberEntity);

        Member member = new Member();
        member.toMember(memberEntity);

        return member;
    }
    public Map<String, String> withdraw(Auth.SignIn member) {
        MemberEntity memberEntity = authenticate(member);
        memberEntity.setMemberStatus(MemberStatus.WITHDRAW);
        memberRepository.save(memberEntity);

        Map<String, String> result = new HashMap<>();
        result.put("id", member.getUsername());
        result.put("result", "withdraw success!");

        return result;
    }

    public Wallet.Card cardCharge(int price, String userName){
        var user = this.memberRepository.findByUsername(userName)
                .orElseThrow(() -> new NoUserException());

        int chargedPrice = user.getCard().getPrice() + price;
        user.getCard().setPrice(chargedPrice);
        user.getCard().setChargedDate(LocalDateTime.now());

        memberRepository.save(user);

        return user.getCard();
    }

    public Wallet getWallet(String userName){
        var user = this.memberRepository.findByUsername(userName)
                .orElseThrow(() -> new NoUserException());

        Wallet wallet = new Wallet();
        wallet.setWallet(user.getCard(), user.getCoupon(), user.getPoint());

        return wallet;
    }
}
