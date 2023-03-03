package com.zerobase.order_drinks.member.service.lmp;

import com.zerobase.order_drinks.components.MailComponents;
import com.zerobase.order_drinks.member.entity.Member;
import com.zerobase.order_drinks.member.exception.MemberNoEmailAuthException;
import com.zerobase.order_drinks.member.model.MemberInput;
import com.zerobase.order_drinks.member.model.MemberStatus;
import com.zerobase.order_drinks.member.repository.MemberRepository;
import com.zerobase.order_drinks.member.service.MemberService;
import com.zerobase.order_drinks.wallet.entity.Card;
import com.zerobase.order_drinks.wallet.entity.Coupon;
import com.zerobase.order_drinks.wallet.entity.Point;
import com.zerobase.order_drinks.wallet.repository.CardRepository;
import com.zerobase.order_drinks.wallet.repository.CouponRepository;
import com.zerobase.order_drinks.wallet.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MemberServiceImp implements MemberService {

    private final MemberRepository memberRepository;
    private final MailComponents mailComponents;
    private final CardRepository cardRepository;
    private final CouponRepository couponRepository;
    private final PointRepository pointRepository;


    @Override
    public boolean register(MemberInput parameter) {

        boolean result = memberRepository.existsByUserId(parameter.getUserId());

        if(result){
            return false;
        }

        String encPassword = BCrypt.hashpw(parameter.getPassword(), BCrypt.gensalt());

        String uuid = UUID.randomUUID().toString();

        Member member = Member.builder()
                .userId(parameter.getUserId())
                .userName(parameter.getUserName())
                .phone(parameter.getPhone())
                .password(encPassword)
                .regDt(LocalDateTime.now())
                .emailAuthYn(false)
                .emailAuthKey(uuid)
                .userStatus(String.valueOf(MemberStatus.REQ))
                .build();

        memberRepository.save(member);

        createWallet(member.getUserId());

        String email = parameter.getUserId();
        String subject = "Welcome!!";
        String text = "<p>회원가입을 축하드립니다. 아래 링크를 클릭하여 회원가입을 완료 하세요</p> " +
                "<div> <a href = 'http://localhost:8080/member/email-auth?id="+uuid+"'> 링크</a> </div>";

        mailComponents.sendMail(email, subject, text);

        return true;
    }

    public boolean createWallet (String userId){
        Card card = Card.builder()
                .price(0)
                .chargedDT(LocalDateTime.now())
                .userId(userId)
                .build();

        cardRepository.save(card);

        Point point = Point.builder()
                .count(0)
                .updateDT(LocalDateTime.now())
                .userId(userId)
                .build();

        pointRepository.save(point);

        Coupon coupon = Coupon.builder()
                .count(0)
                .updateDT(LocalDateTime.now())
                .userId(userId)
                .build();

        couponRepository.save(coupon);

        return true;
    }

    @Override
    public boolean emailAuth(String uuid) {
        Optional<Member> optionalMember = memberRepository.findByEmailAuthKey(uuid);
        if(!optionalMember.isPresent()){
            return false;
        }

        Member member = optionalMember.get();
        member.setEmailAuthDt(LocalDateTime.now());
        member.setEmailAuthYn(true);
        member.setUserStatus(String.valueOf(MemberStatus.ING));
        memberRepository.save(member);

        return true;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Member> optionalMember = memberRepository.findByUserId(username);
        if(!optionalMember.isPresent()){
            throw new UsernameNotFoundException("회원 정보가 존재하지 않습니다.");
        }

        Member member = optionalMember.get();

        if(!member.isEmailAuthYn()){
            throw new MemberNoEmailAuthException();
        }
        else if(member.getUserStatus().equals(String.valueOf(MemberStatus.WITHDRAW))){
            throw new RuntimeException("탈퇴한 회원 입니다.");
        }

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        member.setLastLoginDt(LocalDateTime.now());
        memberRepository.save(member);

        return new User(member.getUserId(), member.getPassword(), grantedAuthorities);
    }
}
