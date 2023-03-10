package com.zerobase.order_drinks.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zerobase.order_drinks.model.constants.EmailAuthStatus;
import com.zerobase.order_drinks.model.constants.MemberStatus;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "MEMBER")
public class MemberEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique=true)
    private String username;

    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    private EmailAuthStatus emailAuthStatus;
    private String emailAuthKey;
    private LocalDateTime emailAuthDateTime;

    @Enumerated(EnumType.STRING)
    private MemberStatus memberStatus;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles;

    private LocalDateTime registerDateTime;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "CARD_ID")
    private WalletEntity.Card card;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "POINT_ID")
    private WalletEntity.Point point;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "COUPON_ID")
    private WalletEntity.Coupon coupon;




    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
