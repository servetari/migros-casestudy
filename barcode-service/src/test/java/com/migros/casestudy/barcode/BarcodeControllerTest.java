package com.migros.casestudy.barcode;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.kafka.topics.barcode-events=barcode-events")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BarcodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void openApi_shouldDescribeBarcodeEndpoint() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Barcode Service API"))
                .andExpect(jsonPath("$.paths['/api/v1/barcodes']").exists());
    }
}