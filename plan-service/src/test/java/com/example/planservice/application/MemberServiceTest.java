package com.example.planservice.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.planservice.application.dto.MemberRegisterResponse;
import com.example.planservice.domain.member.Member;
import com.example.planservice.exception.ApiException;
import com.example.planservice.exception.ErrorCode;
import com.example.planservice.presentation.dto.request.MemberRegisterRequest;
import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class MemberServiceTest {
    @Autowired
    MemberService memberService;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("멤버를 등록한다")
    void testRegister() throws Exception {
        // given
        String email = "a@naver.ocm";
        String profileUri = "https:/sd2sw2.com";
        String name = "김태훈";
        boolean receiveEmails = false;

        MemberRegisterRequest request = MemberRegisterRequest.builder()
            .email(email)
            .profileUri(profileUri)
            .name(name)
            .receiveEmails(receiveEmails)
            .build();

        // when
        MemberRegisterResponse response = memberService.register(request);

        // then
        assertThat(response.getId()).isNotNull();
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getProfileUri()).isEqualTo(profileUri);
        assertThat(response.getName()).isEqualTo(name);
        assertThat(response.isReceiveEmails()).isEqualTo(receiveEmails);

        Member result = em.find(Member.class, response.getId());
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getProfileUri()).isEqualTo(profileUri);
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.isReceiveEmails()).isEqualTo(receiveEmails);
    }

    @Test
    @DisplayName("중복된 이메일로 멤버를 등록할 수 없다")
    void testRegisterFailDuplicatedEmail() throws Exception {
        // given
        String duplicatedEmail = "a@naver.com";
        Member member = Member.builder().email(duplicatedEmail).build();
        em.persist(member);

        MemberRegisterRequest request = MemberRegisterRequest.builder()
            .email(duplicatedEmail)
            .build();
        // when & then
        assertThatThrownBy(() -> memberService.register(request))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining(ErrorCode.ALREADY_REGISTERED.getMessage());
    }


}
