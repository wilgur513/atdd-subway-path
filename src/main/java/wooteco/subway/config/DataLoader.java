package wooteco.subway.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import wooteco.subway.domain.line.Line;
import wooteco.subway.domain.member.Member;
import wooteco.subway.domain.section.Section;
import wooteco.subway.domain.station.Station;
import wooteco.subway.infrastructure.Sha256Hasher;
import wooteco.subway.repository.LineDao;
import wooteco.subway.repository.MemberDao;
import wooteco.subway.repository.SectionDao;
import wooteco.subway.repository.StationDao;

@Component
@Profile("!test")
public class DataLoader implements CommandLineRunner {
    private final StationDao stationDao;
    private final LineDao lineDao;
    private final SectionDao sectionDao;
    private final MemberDao memberDao;

    public DataLoader(StationDao stationDao, LineDao lineDao, SectionDao sectionDao, MemberDao memberDao) {
        this.stationDao = stationDao;
        this.lineDao = lineDao;
        this.sectionDao = sectionDao;
        this.memberDao = memberDao;
    }

    @Override
    public void run(String... args) {
        Station 강남역 = stationDao.insert(new Station("강남역"));
        Station 판교역 = stationDao.insert(new Station("판교역"));
        Station 정자역 = stationDao.insert(new Station("정자역"));
        Station 역삼역 = stationDao.insert(new Station("역삼역"));
        Station 잠실역 = stationDao.insert(new Station("잠실역"));

        Line 신분당선 = lineDao.insert(new Line("신분당선", "red lighten-1"));
        신분당선.addSection(new Section(강남역, 판교역, 10));
        신분당선.addSection(new Section(판교역, 정자역, 10));
        sectionDao.insertSections(신분당선);

        Line 이호선 = lineDao.insert(new Line("2호선", "green lighten-1"));
        이호선.addSection(new Section(강남역, 역삼역, 10));
        이호선.addSection(new Section(역삼역, 잠실역, 10));
        sectionDao.insertSections(이호선);

        Member member = new Member("email@email.com", "password", 10);
        member.newInstanceWithHashPassword(s -> new Sha256Hasher().hashing(s));
        memberDao.insert(member);
    }
}
