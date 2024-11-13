package com.itm.space.backendresources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.controller.UserController;
import com.itm.space.backendresources.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test_name", password = "test_pass", authorities = "ROLE_MODERATOR")
public class UserControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;



    private UUID userId;
    private UserRequest userRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userRequest = new UserRequest(
                "testuser",
                "testuser@gmail.com",
                "password",
                "Test",
                "User");
        userResponse = new UserResponse(
                "Test",
                "User",
                "testuser@gmail.com",
                List.of("ROLE_MODERATOR"),
                List.of("GROUP_MODERATOR"));
    }

    @Test
    void testGetUserById_withRoleModerator() throws Exception {
        when(userService.getUserById(userId)).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.email").value("testuser@gmail.com"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_MODERATOR"))
                .andExpect(jsonPath("$.groups[0]").value("GROUP_MODERATOR"));
    }
    @Test
    void testCreateUser_withRoleModerator() throws Exception {
        String jsonRequest = objectMapper.writeValueAsString(userRequest);
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)).andExpect(status()
                .isOk());
        verify(userService, times(1)).createUser(userRequest);
    }
    @Test
    void testHello_withRoleModerator() throws Exception {
        // Выполняем GET-запрос
        MvcResult result = mockMvc.perform(get("/api/users/hello"))
                .andExpect(status().isOk())
                .andReturn();

        // Проверяем содержимое ответа
        String responseContent = result.getResponse().getContentAsString();
        assertEquals("test_name", responseContent);
    }

}
