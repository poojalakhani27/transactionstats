package com.n26.transactionstats;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n26.transactionstats.Application;
import com.n26.transactionstats.domain.Summary;
import com.n26.transactionstats.domain.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@TestPropertySource("classpath:application.properties")
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
public class TransactionStatisticsIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void shouldCreateTransaction() throws Exception {
        Transaction transaction = new Transaction(12.50, Instant.now().toEpochMilli());
        ObjectMapper objectMapper = new ObjectMapper();
        String requestJsonString = objectMapper.writeValueAsString(transaction);

        this.mockMvc.perform(post("/transactions").
                contentType(MediaType.APPLICATION_JSON)
                .content(requestJsonString))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));
    }

    @Test
    public void shouldReturnBadRequestOnInvalidTransaction() throws Exception {
        Transaction transaction = new Transaction(null, Instant.now().toEpochMilli());
        ObjectMapper objectMapper = new ObjectMapper();
        String requestJsonString = objectMapper.writeValueAsString(transaction);

        this.mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJsonString))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnNoContentIfTransactionOlderThan60Secs() throws Exception {
        long timestampBefore60Secs = Instant.now()
                .minusSeconds(60)
                .toEpochMilli();
        Transaction transaction = new Transaction(12.50, timestampBefore60Secs);
        ObjectMapper objectMapper = new ObjectMapper();
        String requestJsonString = objectMapper.writeValueAsString(transaction);

        this.mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJsonString)).andExpect(status().isNoContent())
                .andExpect(content().string(""));

    }

    @Test
    public void shouldReturnStatistics() throws Exception {
        Transaction transaction1 = new Transaction(12.50, Instant.now().toEpochMilli());
        Thread.sleep(1000);
        Transaction transaction2 = new Transaction(7.50, Instant.now().toEpochMilli());
        ObjectMapper objectMapper = new ObjectMapper();
        String requestJsonString = objectMapper.writeValueAsString(transaction1);

        this.mockMvc.perform(post("/transactions").
                contentType(MediaType.APPLICATION_JSON)
                .content(requestJsonString))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));

        requestJsonString = objectMapper.writeValueAsString(transaction2);
        this.mockMvc.perform(post("/transactions").
                contentType(MediaType.APPLICATION_JSON)
                .content(requestJsonString))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));

        MvcResult mvcResult = this.mockMvc.perform(get("/statistics").
                contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();
        Summary summary = objectMapper.readValue(response, Summary.class);

        assertEquals((Double) 20.0, summary.getSum());
        assertEquals((Double) 7.50, summary.getMin());
        assertEquals((Double) 12.50, summary.getMax());
        assertEquals((Double) 10.0, summary.getAvg());
        assertEquals((Long) 2L, summary.getCount());

    }

    @Test
    public void shouldReturnZeroSummaryStatisticsWhenNoTransaction() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/statistics").
                contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String response = mvcResult.getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        Summary summary = objectMapper.readValue(response, Summary.class);

        assertEquals((Double) 0.0, summary.getSum());
        assertEquals((Double) Double.MAX_VALUE, summary.getMin());
        assertEquals((Double) 0.0, summary.getMax());
        assertEquals((Double) 0.0, summary.getAvg());
        assertEquals((Long) 0L, summary.getCount());
    }

    @Test
    public void shouldReturnStatisticsIf2TransactionsOccuredAtTheSameTime() throws Exception {
        long now = Instant.now().toEpochMilli();
        Transaction transaction1 = new Transaction(12.50, now);
        Transaction transaction2 = new Transaction(7.50, now);
        ObjectMapper objectMapper = new ObjectMapper();
        String requestJsonString = objectMapper.writeValueAsString(transaction1);

        this.mockMvc.perform(post("/transactions").
                contentType(MediaType.APPLICATION_JSON)
                .content(requestJsonString))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));

        requestJsonString = objectMapper.writeValueAsString(transaction2);
        this.mockMvc.perform(post("/transactions").
                contentType(MediaType.APPLICATION_JSON)
                .content(requestJsonString))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));

        MvcResult mvcResult = this.mockMvc.perform(get("/statistics").
                contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String response = mvcResult.getResponse().getContentAsString();

        Summary summary = objectMapper.readValue(response, Summary.class);

        assertEquals((Double) 20.0, summary.getSum());
        assertEquals((Double) 7.50, summary.getMin());
        assertEquals((Double) 12.50, summary.getMax());
        assertEquals((Double) 10.0, summary.getAvg());
        assertEquals((Long) 2L, summary.getCount());

    }
}
