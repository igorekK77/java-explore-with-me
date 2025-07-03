package ru.practicum.main.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.main.users.UserController;
import ru.practicum.main.users.UserService;
import ru.practicum.main.users.dto.CreateUserDto;
import ru.practicum.main.users.dto.UserDto;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    MockMvc mockMvc;

    ObjectMapper objectMapper;

    private UserDto userDto;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
        userDto = new UserDto(1L, "test@yandex.ru", "test");
    }

    @Test
    void testCreateUser() throws Exception {
        CreateUserDto createUserDto = new CreateUserDto("test@yandex.ru", "test");
        String jsonCreateUser = objectMapper.writeValueAsString(createUserDto);
        when(userService.createUser(createUserDto)).thenReturn(userDto);
        mockMvc.perform(post("/admin/users").content(jsonCreateUser).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("test"))
                .andExpect(jsonPath("$.email").value("test@yandex.ru"));
        verify(userService, times(1)).createUser(createUserDto);
    }

    @Test
    void testGetUser() throws Exception {
        UserDto userDto2 = new UserDto(2L, "test2@yandex.ru", "test2");
        when(userService.getUsers(List.of(1L, 2L), 1, 2)).thenReturn(List.of(userDto, userDto2));
        mockMvc.perform(get("/admin/users?ids=1,2&from=1&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("test"))
                .andExpect(jsonPath("$[0].email").value("test@yandex.ru"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("test2"))
                .andExpect(jsonPath("$[1].email").value("test2@yandex.ru"));
        verify(userService, times(1)).getUsers(List.of(1L, 2L), 1, 2);
    }

    @Test
    void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/admin/users/1"))
                .andExpect(status().isNoContent());
    }
}
