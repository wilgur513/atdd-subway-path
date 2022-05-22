package wooteco.subway.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import wooteco.subway.domain.Path;
import wooteco.subway.domain.Section;
import wooteco.subway.domain.SectionEdge;
import wooteco.subway.domain.Station;

public class JGraphtSubwayGraphTest {

    private List<Station> stations;
    private List<Section> sections;
    private JGraphtSubwayGraph graph;

    @BeforeEach
    void setUp() {
        stations = List.of(
            new Station(1L, "강남역"),
            new Station(2L, "잠실역"),
            new Station(3L, "선릉역"),
            new Station(4L, "역삼역"),
            new Station(5L, "뚝섬역"),
            new Station(6L, "잠실새내역"),
            new Station(7L, "아산역"),
            new Station(8L, "부산역"),
            new Station(9L, "서면역")
        );
        sections = List.of(
            new Section(1L, 1L, new SectionEdge(1L, 2L, 5)),
            new Section(2L, 1L, new SectionEdge(2L, 3L, 4)),
            new Section(3L, 2L, new SectionEdge(2L, 4L, 3)),
            new Section(4L, 1L, new SectionEdge(3L, 5L, 6)),
            new Section(5L, 1L, new SectionEdge(5L, 6L, 6)),
            new Section(6L, 1L, new SectionEdge(6L, 7L, 37)),
            new Section(7L, 3L, new SectionEdge(8L, 9L, 4))
        );

        graph = new JGraphtSubwayGraph(stations, sections);
    }

    @DisplayName("환승없는 구간 지하철 경로 찾기")
    @Test
    void searchNotTransferLinePath() {
        Path path = graph.search(stations.get(0), stations.get(6));

        assertThat(path.getStations())
            .containsExactly(stations.get(0), stations.get(1), stations.get(2), stations.get(4),
                stations.get(5), stations.get(6));
        assertThat(path.getSections())
            .containsExactly(sections.get(0), sections.get(1), sections.get(3), sections.get(4),
                sections.get(5));
        assertThat(path.getDistance()).isEqualTo(58);
    }

    @DisplayName("환승 구간이 있는 지하철역 경로 찾기")
    @Test
    void searchTransferLinePath() {
        Path path = graph.search(stations.get(0), stations.get(3));

        assertThat(path.getStations())
            .containsExactly(stations.get(0), stations.get(1), stations.get(3));
        assertThat(path.getSections()).containsExactly(sections.get(0), sections.get(2));
        assertThat(path.getDistance()).isEqualTo(8);
    }

    @DisplayName("경로를 찾을 수 없는 경우 빈 리스트 반환")
    @Test
    void searchNotReachablePath() {
        Path path = graph.search(stations.get(0), stations.get(7));

        assertThat(path.isEmpty()).isTrue();
        assertThat(path.getStations()).isEmpty();
        assertThat(path.getSections()).isEmpty();
        assertThat(path.getDistance()).isEqualTo(0);
    }
}
