//package com.application.facedec;
//
//import com.application.facedec.dto.LoginRequest;
//import com.application.facedec.dto.LoginResponse;
//import com.application.facedec.entity.Role;
//import com.application.facedec.entity.RoleName;
//import com.application.facedec.entity.Employee;
//import com.application.facedec.repository.RoleRepository;
//import com.application.facedec.repository.UserRepository;
//import com.application.facedec.service.JWTService;
//import com.application.facedec.service.UserService;
//import org.junit.Test;
//import org.mockito.Mock;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.ResultActions;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//public class UserControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private JWTService jwtService;
//
//    @Autowired
//    private UserService userService;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private RoleRepository roleRepository;
//
//    @Autowired
//    private LoginResponse loginResponse;
//
//    @Autowired
//    private LoginRequest loginRequest;
//
//    @Autowired
//    private ObjectMapper objectMapper; // Inject ObjectMapper directly
//
//    @Test
//    public void testLoginSuccess() throws Exception {
//        // Create a test user (replace with your actual user creation logic)
//        Employee user = new Employee();
//        user.setEmail("tester@gmail.com");
//        user.setPassword("password");
//
//        Role userRole = roleRepository.findByRoleName(String.valueOf(RoleName.USER))
//                .orElseGet(() -> {
//                    Role newRole = new Role();
//                    newRole.setRoleName(RoleName.USER);
//                    return roleRepository.save(newRole);
//                });
//
//        user.getRoles().add(userRole);
//
//        userRepository.save(user);
//
//        // Create a LoginRequest object
//        LoginRequest loginRequest = new LoginRequest();
//        loginRequest.setEmail(user.getEmail());
//        loginRequest.setPassword(user.getPassword());
//
//        // Perform login request
//        ResultActions response = mockMvc.perform(post("/api/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(loginRequest)))
//                .andExpect(status().isOk());
//
//        // Extract the JWT token from the response
//        String jwtToken = extractJwtFromResponse(response);
//
//        // Assert that the JWT token is not empty
//        assertNotNull(jwtToken);
//    }
//
//    private String extractJwtFromResponse(ResultActions response) throws Exception {
//        String responseBody = response.andReturn().getResponse().getContentAsString();
//        // Parse the response body (assuming LoginResponse contains the token)
//        LoginResponse loginResponse = objectMapper.readValue(responseBody, LoginResponse.class);
//        return loginResponse.getToken();
//    }
//}
