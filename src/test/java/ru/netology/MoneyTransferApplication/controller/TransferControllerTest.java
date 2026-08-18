package ru.netology.MoneyTransferApplication.controller;

import ru.netology.MoneyTransferApplication.model.ConfirmationRequest;
import ru.netology.MoneyTransferApplication.model.TransferRequest;
import ru.netology.MoneyTransferApplication.model.TransferResponse;
import ru.netology.MoneyTransferApplication.service.TransferService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransferController.class)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferService transferService;

    @Autowired
    private ObjectMapper objectMapper;

    private TransferRequest validRequest;
    private TransferResponse response;

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

        response = new TransferResponse("test-operation-id");
    }

    @Test
    void transfer_Success_Returns200() throws Exception {
        when(transferService.transfer(any(TransferRequest.class))).thenReturn(response);

        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operationId").value("test-operation-id"));
    }

    @Test
    void confirm_Success_Returns200() throws Exception {
        ConfirmationRequest confirmation = new ConfirmationRequest("test-id", "1234");

        when(transferService.confirm(any(ConfirmationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/confirmOperation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operationId").value("test-operation-id"));
    }

    @Test
    void health_Returns200() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Service is running"));
    }
}