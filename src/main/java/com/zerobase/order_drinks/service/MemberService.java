package com.zerobase.order_drinks.service;

import com.zerobase.order_drinks.components.MailComponent;
import com.zerobase.order_drinks.exception.impl.member.*;
import com.zerobase.order_drinks.model.Auth;
import com.zerobase.order_drinks.model.MemberEntity;
import com.zerobase.order_drinks.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

        String uuid = UUID.randomUUID().toString();

        var result = this.memberRepository.save(member.toEntity(uuid));

        String email = member.getUsername();
        String subject = "Welcome!!";
        String text = "<p>회원가입을 축하드립니다. 아래 링크를 클릭하여 회원가입을 완료 하세요</p> " +
                "<div> <a href = 'http://localhost:8080/auth/email-auth?id="+uuid+"'> 링크</a> </div>";

        mailComponent.sendMail(email, subject, text);

        return result;
    }

    public static boolean isValidEmail(String email) {
        boolean err = false;
        String regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        if(m.matches()) {
            err = true;
        }
        return err;
    }

    public MemberEntity authenticate(Auth.SignIn member){
        var user = this.memberRepository.findByUsername(member.getUsername())
                .orElseThrow(() -> new NoUserException());

        if(!this.passwordEncoder.matches(member.getPassword(), user.getPassword())){
            throw new PasswordNotMatchException();
        }
        else if(!user.isEmailAuthStatus()){
            throw new NoEmailAuthException();
        }

        return user;
    }

    public MemberEntity emailAuth(String uuid){
        Optional<MemberEntity> optionalMember = memberRepository.findByEmailAuthKey(uuid);

        if(!optionalMember.isPresent()){
            throw new NoUserException();
        }

        MemberEntity member = optionalMember.get();
        member.setEmailAuthDate(LocalDateTime.now());
        member.setEmailAuthStatus(true);
        memberRepository.save(member);

        return member;
    }
}
