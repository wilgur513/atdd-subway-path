package wooteco.subway.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@DisplayName("지하철 노선 관련 기능")
public class LineAcceptanceTest extends AcceptanceTest {

    @DisplayName("지하철 노선 이름에 빈 문자열을 사용할 수 없다")
    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "     "})
    void createLineWithEmptyName(String name) {
        long upStationId = requestCreateStation("강남역").jsonPath().getLong("id");
        long downStationId = requestCreateStation("역삼역").jsonPath().getLong("id");

        ExtractableResponse<Response> response = requestCreateLine(name, "bg-red-600", upStationId,
            downStationId, 10, 0);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("지하철 노선 색깔에 빈 문자열을 사용할 수 없다")
    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "     "})
    void createLineWithEmptyColor(String color) {
        long upStationId = requestCreateStation("강남역").jsonPath().getLong("id");
        long downStationId = requestCreateStation("역삼역").jsonPath().getLong("id");

        ExtractableResponse<Response> response = requestCreateLine("신분당선", color, upStationId,
            downStationId, 10, 0);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("지하철 노선 등록")
    @Test
    void createLine() {
        String name = "신분당선";
        String color = "bg-red-600";
        int extraFare = 900;
        long upStationId = requestCreateStation("강남역").jsonPath().getLong("id");
        long downStationId = requestCreateStation("역삼역").jsonPath().getLong("id");

        ExtractableResponse<Response> response = requestCreateLine(name, color, upStationId,
            downStationId, 10, extraFare);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.header("Location")).isNotBlank();
        assertThatCode(() -> response.jsonPath().getLong("id")).doesNotThrowAnyException();
        assertThat(response.jsonPath().getString("name")).isEqualTo(name);
        assertThat(response.jsonPath().getString("color")).isEqualTo(color);
        assertThat(response.jsonPath().getList("stations.id", Long.class))
            .containsExactlyInAnyOrder(upStationId, downStationId);
        assertThat(response.jsonPath().getInt("extraFare")).isEqualTo(extraFare);
    }

    @DisplayName("지하철 노선 중복 등록을 허용하지 않는다")
    @Test
    void createLineWithDuplicateName() {
        // given
        String name = "신분당선";
        String color = "bg-red-600";
        long upStationId = requestCreateStation("강남역").jsonPath().getLong("id");
        long downStationId = requestCreateStation("역삼역").jsonPath().getLong("id");

        requestCreateLine(name, color, upStationId, downStationId, 10, 0);

        // when

        ExtractableResponse<Response> response = requestCreateLine(name, color, upStationId,
            downStationId, 10, 0);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("지하철 노선은 존재하지 않는 상행 또는 하행 역으로 등록할 수 없다.")
    @Test
    void createLineWithNotExistStation() {
        ExtractableResponse<Response> response = requestCreateLine("신분당선", "bg-red-600", 1L, 2L, 10,
            0);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @DisplayName("지하철 노선 조회")
    @Test
    void showLineById() {
        // given
        String name = "신분당선";
        String color = "bg-red-600";
        long upStationId = requestCreateStation("강남역").jsonPath().getLong("id");
        long downStationId = requestCreateStation("역삼역").jsonPath().getLong("id");

        ExtractableResponse<Response> createResponse = requestCreateLine(name, color, upStationId,
            downStationId, 10, 0);
        Long createdId = createResponse.jsonPath().getLong("id");

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .when().get("/lines/" + createdId)
            .then().log().all()
            .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.jsonPath().getLong("id")).isNotNull();
        assertThat(response.jsonPath().getString("name")).isEqualTo(name);
        assertThat(response.jsonPath().getString("color")).isEqualTo(color);
        assertThat(response.jsonPath().getList("stations.id", Long.class))
            .containsExactlyInAnyOrder(upStationId, downStationId);
    }

    @DisplayName("존재하지 않는 지하철 노선 조회시 예외를 반환한다")
    @Test
    void showNotExistLine() {
        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .when().get("/lines/" + 1L)
            .then().log().all()
            .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @DisplayName("지하철 노선 목록 조회")
    @Test
    void showLines() {
        // given
        long upStationId = requestCreateStation("강남역").jsonPath().getLong("id");
        long downStationId = requestCreateStation("역삼역").jsonPath().getLong("id");

        ExtractableResponse<Response> createResponse1 = requestCreateLine("신분당선", "bg-red-600",
            upStationId, downStationId, 10, 0);

        ExtractableResponse<Response> createResponse2 = requestCreateLine("1호선", "bg-blue-600",
            upStationId, downStationId, 10, 0);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .when().get("/lines")
            .then().log().all()
            .extract();

        // then
        List<Long> expectedLineIds = Arrays.asList(createResponse1, createResponse2).stream()
            .map(it -> it.jsonPath().getObject("id", Long.class))
            .collect(Collectors.toList());
        List<Long> actualLineIds = response.jsonPath().getList("id", Long.class);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(expectedLineIds).containsExactlyInAnyOrderElementsOf(actualLineIds);
    }

    @DisplayName("이름이 공백인 지하철 노선을 수정할 수 없다")
    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "     "})
    void canNotUpdateLineWithEmptyName(String lineName) {
        // given
        long upStationId = requestCreateStation("강남역").jsonPath().getLong("id");
        long downStationId = requestCreateStation("역삼역").jsonPath().getLong("id");

        ExtractableResponse<Response> response = requestCreateLine("신분당선", "bg-red-600",
            upStationId, downStationId, 10, 0);
        long createdId = response.jsonPath().getLong("id");

        // when & then
        RestAssured.given().log().all()
            .body(Map.of("name", lineName, "color", "bg-red-600"))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .put("/lines/" + createdId)
            .then().log().all()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("색깔이 공백인 지하철 노선을 수정할 수 없다")
    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "     "})
    void canNotUpdateLineWithEmptyColor(String lineColor) {
        // given
        long 강남역_id = requestCreateStation("강남역").jsonPath().getLong("id");
        long 역삼역_id = requestCreateStation("역삼역").jsonPath().getLong("id");
        long 신분당선_id = requestCreateLine("신분당선", "bg-red-600", 강남역_id, 역삼역_id,
            10, 0).jsonPath().getLong("id");

        // when & then
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .body(Map.of("name", "신분당선", "color", lineColor))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .put("/lines/" + 신분당선_id)
            .then().log().all()
            .extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.jsonPath().getString("message")).isNotBlank();
    }

    @DisplayName("지하철 노선 수정")
    @Test
    void updateLine() {
        // given
        long upStationId = requestCreateStation("강남역").jsonPath().getLong("id");
        long downStationId = requestCreateStation("역삼역").jsonPath().getLong("id");
        String newName = "1호선";
        String newColor = "bg-blue-600";
        int newExtraFare = 900;

        ExtractableResponse<Response> createResponse = requestCreateLine("신분당선", "bg-red-600",
            upStationId, downStationId, 10, 0);
        long createdId = createResponse.jsonPath().getLong("id");

        // when
        RestAssured.given().log().all()
            .body(Map.of("name", newName, "color", newColor, "extraFare", newExtraFare))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .put("/lines/" + createdId)
            .then().log().all()
            .statusCode(HttpStatus.OK.value());

        // then
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when().get("/lines/" + createdId)
            .then().log().all()
            .extract();

        assertThat(response.jsonPath().getLong("id")).isEqualTo(createdId);
        assertThat(response.jsonPath().getString("name")).isEqualTo(newName);
        assertThat(response.jsonPath().getString("color")).isEqualTo(newColor);
        assertThat(response.jsonPath().getInt("extraFare")).isEqualTo(newExtraFare);
    }

    @DisplayName("중복된 노선 이름 수정을 허용하지 않는다")
    @Test
    void canNotUpdateByDuplicationName() {
        // given
        long 강남역_id = requestCreateStation("강남역").jsonPath().getLong("id");
        long 역삼역_id = requestCreateStation("역삼역").jsonPath().getLong("id");
        long 신분당선_id = requestCreateLine("신분당선", "bg-red-600", 강남역_id, 역삼역_id,
            10, 0).jsonPath().getLong("id");
        requestCreateLine("1호선", "bg-red-600", 강남역_id, 역삼역_id, 10, 0);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .body(Map.of("name", "1호선", "color", "bg-red-600"))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .put("/lines/" + 신분당선_id)
            .then().log().all()
            .extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.jsonPath().getString("message")).isNotBlank();
    }

    @DisplayName("존재하지 않는 노선 수정 시도")
    @Test
    void updateNotExistLine() {
        // when
        RestAssured.given().log().all()
            .body(Map.of("name", "1호선", "color", "bg-red-600"))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .put("/lines/50")
            .then().log().all()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @DisplayName("존재하지 않는 지하철 노선을 삭제 시도시 404 반환")
    @Test
    void deleteNotExistLine() {
        // when
        RestAssured.given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .delete("/lines/50")
            .then().log().all()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @DisplayName("지하철 노선을 삭제 시도")
    @Test
    void deleteLine() {
        // given
        long upStationId = requestCreateStation("강남역").jsonPath().getLong("id");
        long downStationId = requestCreateStation("역삼역").jsonPath().getLong("id");

        ExtractableResponse<Response> response = requestCreateLine("신분당선", "bg-red-600",
            upStationId, downStationId, 10, 0);

        requestCreateLine("1호선", "bg-red-600", upStationId, downStationId, 10, 0);
        long createdId = response.jsonPath().getLong("id");

        // when
        RestAssured.given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .delete("/lines/" + createdId)
            .then().log().all()
            .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @DisplayName("지하철 노선 이름은 255자이하여야 한다.")
    @Test
    void createLineWithLongName() {
        ExtractableResponse<Response> response = requestCreateLine("A".repeat(256), "bg-red-600",
            1L, 2L, 10,
            0);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.jsonPath().getString("message")).isNotBlank();
    }

    @DisplayName("지하철 노선 색깔은 20자이하여야 한다.")
    @Test
    void createLineWithLongColor() {
        ExtractableResponse<Response> response = requestCreateLine("신분당선", "A".repeat(21), 1L, 2L,
            10,
            0);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.jsonPath().getString("message")).isNotBlank();
    }

    @DisplayName("지하철 거리는 1이상이여야 한다.")
    @Test
    void createLineWithInvalidDistance() {
        ExtractableResponse<Response> response = requestCreateLine("신분당선", "bg-red-600", 1L, 2L, 0,
            0);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.jsonPath().getString("message")).isNotBlank();
    }

    @DisplayName("추가 요금은 0이상이여야 한다.")
    @Test
    void createLineWithInvalidAge() {
        ExtractableResponse<Response> response = requestCreateLine("신분당선", "bg-red-600", 1L, 2L, 10,
            -1);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.jsonPath().getString("message")).isNotBlank();
    }
}
