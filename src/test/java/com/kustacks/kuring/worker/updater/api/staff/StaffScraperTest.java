package com.kustacks.kuring.worker.updater.api.staff;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kustacks.kuring.worker.config.JsonConfig;
import com.kustacks.kuring.worker.config.MappedBeanConfig;
import com.kustacks.kuring.worker.error.ErrorCode;
import com.kustacks.kuring.worker.error.InternalLogicException;
import com.kustacks.kuring.worker.updater.api.scrap.NormalJsoupClient;
import com.kustacks.kuring.worker.updater.api.staff.dto.TestStaffDTO;
import com.kustacks.kuring.worker.updater.deptinfo.DeptInfo;
import com.kustacks.kuring.worker.updater.deptinfo.art_design.CommunicationDesignDept;
import com.kustacks.kuring.worker.updater.deptinfo.art_design.LivingDesignDept;
import com.kustacks.kuring.worker.updater.deptinfo.liberal_art.KoreanDept;
import com.kustacks.kuring.worker.updater.deptinfo.real_estate.RealEstateDept;
import com.kustacks.kuring.worker.updater.api.scrap.StaffScraper;
import com.kustacks.kuring.worker.updater.api.scrap.parser.EachDeptStaffHTMLParser;
import com.kustacks.kuring.worker.updater.api.scrap.parser.KuStaffHTMLParser;
import com.kustacks.kuring.worker.updater.api.scrap.parser.RealEstateStaffHTMLParser;
import com.kustacks.kuring.worker.updater.staff.dto.StaffDTO;
import org.junit.jupiter.api.*;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringJUnitConfig({
        StaffScraper.class,
        EachDeptStaffAPIClient.class, KuStaffAPIClient.class, RealEstateStaffAPIClient.class,
        EachDeptStaffHTMLParser.class, KuStaffHTMLParser.class, RealEstateStaffHTMLParser.class,
        NormalJsoupClient.class,
        KoreanDept.class, LivingDesignDept.class, CommunicationDesignDept.class, RealEstateDept.class,
        MappedBeanConfig.StaffMappedBeanConfig.class, JsonConfig.class
})
@TestPropertySource("classpath:test-constants.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class StaffScraperTest {

    @Value("${staff.each-dept-url}")
    private String eachDeptUrl;

    @Value("${staff.living-design-url}")
    private String livingDesignDeptUrl;

    @Value("${staff.real-estate-url}")
    private String realEstateUrl;

    private final StaffScraper staffScraper;

    private final KoreanDept koreanDept;
    private final LivingDesignDept livingDesignDept;
    private final RealEstateDept realEstateDept;

    private final ObjectMapper objectMapper;

    private static ClientAndServer mockServer;
    private static final int MOCK_SERVER_PORT = 9000;

    private final String realEstateFolderName = "real_estate";
    private final String livingDesignFolderName = "living_design";
    private final String koreanFolderName = "korean";

    public StaffScraperTest(
            StaffScraper staffScraper,
            ObjectMapper objectMapper,
            KoreanDept koreanDept,
            LivingDesignDept livingDesignDept,
            RealEstateDept realEstateDept) {

        this.staffScraper = staffScraper;
        this.objectMapper = objectMapper;
        this.koreanDept = koreanDept;
        this.livingDesignDept = livingDesignDept;
        this.realEstateDept = realEstateDept;
    }

    @BeforeAll
    static void setUp() {
        mockServer = ClientAndServer.startClientAndServer(MOCK_SERVER_PORT);
    }

    @AfterAll
    static void cleanUp() {
        mockServer.stop();
        // mockServer가 완전히 꺼지지 않으면, 다른 테스트 클래스의 mockServer Start에 영향을 줄 수 있으므로, 완전히 꺼질 때 까지 폴링한다.
        while(!mockServer.hasStopped()) { }
    }

    @Nested
    @DisplayName("성공")
    class Success {

        @Test
        @DisplayName("RealEstate")
        void successForRealEstate() throws URISyntaxException, IOException {

            URI uri = new URI(realEstateUrl);
            String requestPath = uri.getPath();
            testTemplate(requestPath, realEstateDept, realEstateFolderName);
        }

        @Test
        @DisplayName("Ku")
        void successForKu() throws URISyntaxException, IOException {

            URI uri = new URI(livingDesignDeptUrl);
            String requestPath = uri.getPath();
            testTemplate(requestPath, livingDesignDept, livingDesignFolderName);
        }

        @Test
        @DisplayName("EachDept")
        void successForEachDept() throws URISyntaxException, IOException {

            URI uri = new URI(eachDeptUrl);
            String requestPath = uri.getPath();
            testTemplate(requestPath, koreanDept, koreanFolderName);
        }

        private void testTemplate(String requestPath, DeptInfo deptInfo, String folderName) throws IOException {

            // given
            createExpectationForSuccess(requestPath, deptInfo instanceof KoreanDept ? deptInfo.getStaffScrapInfo().getPfForumId().get(0) : "", folderName);

            // when
            List<StaffDTO> staffDTOList = staffScraper.scrap(deptInfo);

            // then
            int correct = 0;
            List<TestStaffDTO> answers = getAnswers("staff/staffpages/" + folderName + "/answers");
            for (StaffDTO result : staffDTOList) {
                for (TestStaffDTO answer : answers) {
                    if(compareDTO(result, answer)) {
                        ++correct;
                        break;
                    }
                }
            }

            assertEquals(answers.size(), correct);
        }

        private void createExpectationForSuccess(String requestPath, String pfForumId, String folderName) throws IOException {

            String responseBody = readHTML("staff/staffpages/" + folderName + "/page.html");

            HttpRequest request = request().withMethod("GET").withPath(requestPath);
            boolean isEachDept = pfForumId.length() > 0;
            if(isEachDept) {
                request.withQueryStringParameter("pfForumId", pfForumId);
            }

            HttpResponse response = response().withStatusCode(200)
                    .withContentType(MediaType.TEXT_HTML_UTF_8)
                    .withBody(responseBody);

            new MockServerClient("127.0.0.1", MOCK_SERVER_PORT)
                    .when(request, exactly(1))
                    .respond(response);
        }

        private List<TestStaffDTO> getAnswers(String answerPath) {

            List<TestStaffDTO> staffDTOList = new LinkedList<>();
            int idx=1;
            while(true) {
                try {
                    String answerStr = readAnswer(answerPath, idx);
                    staffDTOList.add(objectMapper.readValue(answerStr, TestStaffDTO.class));
                    ++idx;
                } catch(IOException e) {
                    return staffDTOList;
                }
            }
        }
    }

    @Nested
    @DisplayName("실패")
    class Fail {

        @Nested
        @DisplayName("HTML 구조 달라짐")
        class HTMLDiff {

            /*
                HTMLParser 실패 테스트
            */
            // 두 번째 교수님 (정의철) 연락처 div 삭제함
            @Test
            @DisplayName("RealEstate")
            void failByHTMLDiffForRealEstate() throws IOException, URISyntaxException {

                URI uri = new URI(realEstateUrl);
                String requestPath = uri.getPath();
                testTemplate(requestPath, realEstateDept, realEstateFolderName);
            }

            // 첫 번째 교수님 (김선미) 이름 제외 column 없음
            @Test
            @DisplayName("Ku")
            void failByHTMLDiffForKu() throws IOException, URISyntaxException {

                URI uri = new URI(livingDesignDeptUrl);
                String requestPath = uri.getPath();
                testTemplate(requestPath, livingDesignDept, livingDesignFolderName);
            }

            // 첫 번째 교수님 (신동흔) 첫 번째 dl태그를 d로 바꿈
            @Test
            @DisplayName("EachDept")
            void failByHTMLDiffForEachDept() throws IOException, URISyntaxException {

                URI uri = new URI(eachDeptUrl);
                String requestPath = uri.getPath();
                testTemplate(requestPath, koreanDept, koreanFolderName);
            }

            private void testTemplate(String requestPath, DeptInfo deptInfo, String folderName) throws IOException {

                // given
                createExpectationForHTMLDiff(requestPath, deptInfo instanceof KoreanDept ? deptInfo.getStaffScrapInfo().getPfForumId().get(0) : "", folderName);

                // when, then
                InternalLogicException e = assertThrows(InternalLogicException.class, () -> staffScraper.scrap(deptInfo));
                assertEquals(ErrorCode.STAFF_SCRAPER_CANNOT_PARSE, e.getErrorCode());
            }

            private void createExpectationForHTMLDiff(String requestPath, String pfForumId, String folderName) throws IOException {

                // 첫 번째 교수님 모든 태그 dd -> dt로 바꿈
                String responseBody = readHTML("staff/staffpages/" + folderName + "/wrong.html");

                HttpRequest request = request().withMethod("GET").withPath(requestPath);
                boolean isEachDept = pfForumId.length() > 0;
                if(isEachDept) {
                    request.withQueryStringParameter("pfForumId", pfForumId);
                }

                HttpResponse response = response().withStatusCode(200)
                        .withContentType(MediaType.TEXT_HTML_UTF_8)
                        .withBody(responseBody);

                new MockServerClient("127.0.0.1", MOCK_SERVER_PORT)
                        .when(request, exactly(1))
                        .respond(response);
            }
        }

        @Nested
        @DisplayName("API 요청에 대한 잘못된 응답")
        class APIError {

            /*
                StaffAPIClient 실패 테스트
             */
            @Test
            @DisplayName("RealEstate")
            void failByAPIErrorForRealEstate() throws URISyntaxException {

                URI uri = new URI(realEstateUrl);
                String requestPath = uri.getPath();
                testTemplate(requestPath, realEstateDept);
            }

            @Test
            @DisplayName("Ku")
            void failByAPIErrorForKu() throws URISyntaxException {

                URI uri = new URI(livingDesignDeptUrl);
                String requestPath = uri.getPath();
                testTemplate(requestPath, livingDesignDept);
            }

            @Test
            @DisplayName("EachDept")
            void failByAPIErrorForEachDept() throws URISyntaxException {

                URI uri = new URI(eachDeptUrl);
                String requestPath = uri.getPath();
                testTemplate(requestPath, koreanDept);
            }

            private void testTemplate(String requestPath, DeptInfo deptInfo) {

                // given
                createExpectationForAPIError(requestPath, deptInfo instanceof KoreanDept ? deptInfo.getStaffScrapInfo().getPfForumId().get(0) : "");

                // when, then
                InternalLogicException e = assertThrows(InternalLogicException.class, () -> staffScraper.scrap(deptInfo));
                assertEquals(ErrorCode.STAFF_SCRAPER_CANNOT_SCRAP, e.getErrorCode());
            }

            private void createExpectationForAPIError(String requestPath, String pfForumId) {

                HttpRequest request = request().withMethod("GET").withPath(requestPath);
                boolean isEachDept = pfForumId.length() > 0;
                if(isEachDept) {
                    request.withQueryStringParameter("pfForumId", pfForumId);
                }

                HttpResponse response = response().withStatusCode(404);

                new MockServerClient("127.0.0.1", MOCK_SERVER_PORT)
                        .when(request, exactly(1))
                        .respond(response);
            }
        }
    }

    private String readHTML(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        return resourceToString(resource.getInputStream());
    }

    private String readAnswer(String path, int idx) throws IOException {
        ClassPathResource resource = new ClassPathResource(path + "/" + idx + ".json");
        return resourceToString(resource.getInputStream());
    }

    private String resourceToString(InputStream inputStream) throws IOException {
        return FileCopyUtils.copyToString(new InputStreamReader(inputStream));
    }

    private boolean compareDTO(StaffDTO result, TestStaffDTO answer) {
        return (result.getName().equals(answer.getName())) &&
                (result.getMajor().equals(answer.getMajor())) &&
                (result.getLab().equals(answer.getLab())) &&
                (result.getPhone().equals(answer.getPhone())) &&
                (result.getEmail().equals(answer.getEmail()));
    }
}
