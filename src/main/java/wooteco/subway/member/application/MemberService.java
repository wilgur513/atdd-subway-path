package wooteco.subway.member.application;

import org.springframework.stereotype.Service;
import wooteco.subway.auth.domain.LoginMember;
import wooteco.subway.auth.infrastructure.JwtTokenProvider;
import wooteco.subway.member.dao.MemberDao;
import wooteco.subway.member.domain.Member;
import wooteco.subway.member.dto.MemberRequest;
import wooteco.subway.member.dto.MemberResponse;

@Service
public class MemberService {
    private final MemberDao memberDao;

    public MemberService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    public MemberResponse createMember(MemberRequest request) {
        Member member = memberDao.insert(request.toMember());
        return MemberResponse.of(member);
    }

    public MemberResponse findMember(Long id) {
        Member member = memberDao.findById(id);
        return MemberResponse.of(member);
    }

    public MemberResponse findMemberOfMine(LoginMember loginMember) {
        return MemberResponse.of(memberDao.findById(loginMember.getId()));
    }

    public MemberResponse updateMemberOfMine(LoginMember loginMember, MemberRequest memberRequest) {
        updateMember(loginMember.getId(), memberRequest);
        return new MemberResponse(loginMember.getId(), memberRequest.getEmail(), memberRequest.getAge());
    }

    public void updateMember(Long id, MemberRequest memberRequest) {
        memberDao.update(new Member(id, memberRequest.getEmail(), memberRequest.getPassword(), memberRequest.getAge()));
    }

    public void deleteMemberOfMine(LoginMember loginMember) {
        deleteMember(loginMember.getId());
    }

    public void deleteMember(Long id) {
        memberDao.deleteById(id);
    }
}
