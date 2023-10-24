package com.example.planservice.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.planservice.domain.label.Label;
import com.example.planservice.domain.member.Member;
import com.example.planservice.domain.memberofplan.MemberOfPlan;
import com.example.planservice.domain.plan.Plan;
import com.example.planservice.exception.ApiException;
import com.example.planservice.exception.ErrorCode;
import com.example.planservice.presentation.dto.request.LabelCreateRequest;
import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LabelServiceTest {
    @Autowired
    LabelService labelService;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("라벨을 생성한다")
    void testCreateLabel() throws Exception {
        // given
        String name = "라벨1";
        Plan plan = createPlanUsingTest();
        Member member = createMemberUsingTest(plan);

        LabelCreateRequest request = LabelCreateRequest.builder()
            .planId(plan.getId())
            .name(name)
            .build();

        // when
        Long createdId = labelService.create(member.getId(), request);

        // then
        Label result = em.find(Label.class, createdId);
        assertThat(result.getName()).isEqualTo(name);
    }

    @Test
    @DisplayName("라벨에는 플랜이 포함되어야 한다")
    void testCreateLabelFailPlanNotFound() throws Exception {
        // given
        String name = "라벨1";
        Member member = createMemberUsingTest(null);

        LabelCreateRequest request = LabelCreateRequest.builder()
            .planId(1231412L)
            .name(name)
            .build();

        // when & then
        assertThatThrownBy(() -> labelService.create(member.getId(), request))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining(ErrorCode.PLAN_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("플랜에 소속된 멤버만 라벨을 생성할 수 있다")
    void testCreateLabelFailMemberNotExistPlan() throws Exception {
        // given
        String name = "라벨1";
        Plan plan = createPlanUsingTest();
        Member member = createMemberUsingTest(null);

        LabelCreateRequest request = LabelCreateRequest.builder()
            .planId(plan.getId())
            .name(name)
            .build();

        // when & then
        assertThatThrownBy(() -> labelService.create(member.getId(), request))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining(ErrorCode.MEMBER_NOT_FOUND_IN_PLAN.getMessage());
    }

    @Test
    @DisplayName("하나의 플랜 안에서는 동일한 이름의 라벨이 등록될 수 없다")
    void testCreateLabelFailNameDuplicated() throws Exception {
        // given
        String duplicatedName = "중복이름";
        Plan plan = createPlanUsingTest();
        Member member = createMemberUsingTest(plan);
        createLabelUsingTest(duplicatedName, plan);

        LabelCreateRequest request = LabelCreateRequest.builder()
            .planId(plan.getId())
            .name(duplicatedName)
            .build();

        // when & then
        assertThatThrownBy(() -> labelService.create(member.getId(), request))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining(ErrorCode.LABEL_NAME_DUPLICATE.getMessage());
    }

    private Label createLabelUsingTest(String name, Plan plan) {
        Label label = Label.create(name, plan);
        em.persist(label);
        plan.getLabels().add(label);
        return label;
    }

    private Member createMemberUsingTest(Plan plan) {
        Member member = Member.builder().build();
        em.persist(member);
        if (plan == null) {
            return member;
        }
        MemberOfPlan memberOfPlan = MemberOfPlan.builder()
            .plan(plan)
            .member(member)
            .build();
        em.persist(memberOfPlan);
        return member;
    }

    private Plan createPlanUsingTest() {
        Plan plan = Plan.builder().build();
        em.persist(plan);
        return plan;
    }
}