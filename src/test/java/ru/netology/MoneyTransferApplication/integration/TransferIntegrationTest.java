package ru.netology.MoneyTransferApplication.integration;

import ru.netology.MoneyTransferApplication.model.ConfirmationRequest;
import ru.netology.MoneyTransferApplication.model.TransferRequest;
import ru.netology.MoneyTransferApplication.model.TransferResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransferIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private TransferRequest validRequest;

    @BeforeEach
    void setUp() {
        TransferRequest.Amount amount = new TransferRequest.Amount(1000, "RUB");
        validRequest = new TransferRequest(
                "1111222233334444",
                "12/25",
                "123",
                "5555666677778888",
                amount
        );
    }

    @Test
    void testFullTransferFlow() {
        ResponseEntity<TransferResponse> transferResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/transfer",
                validRequest,
                TransferResponse.class
        );

        assertEquals(HttpStatus.OK, transferResponse.getStatusCode());
        assertNotNull(transferResponse.getBody());
        assertNotNull(transferResponse.getBody().getOperationId());

        String operationId = transferResponse.getBody().getOperationId();

        ConfirmationRequest confirmation = new ConfirmationRequest(operationId, "1234");
        ResponseEntity<TransferResponse> confirmResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/confirmOperation",
                confirmation,
                TransferResponse.class
        );

        assertEquals(HttpStatus.OK, confirmResponse.getStatusCode());
        assertNotNull(confirmResponse.getBody());
        assertEquals(operationId, confirmResponse.getBody().getOperationId());
    }

    @Test
    void testTransferWithInvalidCard() {
        TransferRequest invalidRequest = new TransferRequest(
                "1111222233334444",
                "12/25",
                "999",
                "5555666677778888",
                new TransferRequest.Amount(1000, "RUB")
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/transfer",
                invalidRequest,
                String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("message"));
    }

    @Test
    void testHealthCheck() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/health",
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Service is running", response.getBody());
    }
}