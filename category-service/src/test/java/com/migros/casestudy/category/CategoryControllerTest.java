package com.migros.casestudy.category;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAll_shouldReturnAllCategories()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/categories")
                )
                .andExpect(status().isOk())
                .andExpect(
                        content()
                                .contentTypeCompatibleWith(
                                        "application/json"
                                )
                )
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(
                        jsonPath("$[0].code")
                                .value("BK")
                )
                .andExpect(
                        jsonPath("$[0].name")
                                .value("Bakliyat")
                )
                .andExpect(
                        jsonPath("$[1].code")
                                .value("ME")
                )
                .andExpect(
                        jsonPath("$[1].name")
                                .value("Meyve")
                )
                .andExpect(
                        jsonPath("$[2].code")
                                .value("ET")
                )
                .andExpect(
                        jsonPath("$[2].name")
                                .value("Et")
                )
                .andExpect(
                        jsonPath("$[3].code")
                                .value("IC")
                )
                .andExpect(
                        jsonPath("$[3].name")
                                .value("İçecek")
                )
                .andExpect(
                        jsonPath("$[4].code")
                                .value("BL")
                )
                .andExpect(
                        jsonPath("$[4].name")
                                .value("Balık")
                );
    }

    @Test
    void getByCode_shouldReturnCategory_whenCategoryExists()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/v1/categories/{code}",
                                "ME"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.code")
                                .value("ME")
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Meyve")
                );
    }

    @Test
    void getByCode_shouldAcceptLowercaseCode()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/v1/categories/{code}",
                                "me"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.code")
                                .value("ME")
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Meyve")
                );
    }

    @Test
    void getByCode_shouldReturnNotFound_whenCategoryDoesNotExist()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/v1/categories/{code}",
                                "XX"
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Not Found")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Kategori bulunamadı: XX")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/api/v1/categories/XX"
                                )
                )
                .andExpect(
                        jsonPath("$.timestamp")
                                .exists()
                );
    }

    @Test
    void openApi_shouldDescribeCategoryEndpoints()
            throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Category Service API"))
                .andExpect(jsonPath("$.paths['/api/v1/categories']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/categories/{code}']").exists());
    }
}