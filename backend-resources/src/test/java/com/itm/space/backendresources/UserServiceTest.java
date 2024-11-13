package com.itm.space.backendresources;

import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.exception.BackendResourcesException;
import com.itm.space.backendresources.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private Keycloak keycloakMock;

    @MockBean
    private RealmResource realmResourceMock;

    @MockBean
    private UsersResource usersResourceMock;

    @MockBean
    private UserResource userResourceMock;

    @MockBean
    private RoleMappingResource roleMappingResourceMock;

    @MockBean
    private GroupResource groupResourceMock;

    @MockBean
    private MappingsRepresentation mappingsRepresentationMock;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(keycloakMock.realm(any())).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(usersResourceMock.get(any())).thenReturn(userResourceMock);
    }

    @Test
    void testCreateUser() {
        UserRequest user = new UserRequest(
                "testuser",
                "doe@gmail.com",
                "12345",
                "pupa",
                "zalupa"
        );
        when(usersResourceMock.create(any(UserRepresentation.class)))
                .thenReturn(Response.status(Response.Status.CREATED).build());

        assertDoesNotThrow(() -> userService.createUser(user));
    }

    @Test
    void tesCreateUserConflict() {
        UserRequest user = new UserRequest(
                "testuser",
                "doe@gmail.com",
                "12345",
                "pupa",
                "zalupa"
        );

        when(usersResourceMock.create(any(UserRepresentation.class))).thenReturn(
                Response.status(Response.Status.CONFLICT).build());
        assertThrows(BackendResourcesException.class, () -> userService.createUser(user));

    }

    @Test
    void testGetUserByIdSuccess() {
        UUID userId = UUID.randomUUID();
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId(userId.toString());
        userRepresentation.setUsername("testuser");
        userRepresentation.setEmail("testuser@gmail.com");
        userRepresentation.setFirstName("Test");
        userRepresentation.setLastName("Test");
        userRepresentation.setEnabled(false);

        RoleRepresentation roleUserMock = mock(RoleRepresentation.class);
        when(roleUserMock.getName()).thenReturn("ROLE_USER");
        RoleRepresentation roleAdminMock = mock(RoleRepresentation.class);
        when(roleAdminMock.getName()).thenReturn("ROLE_ADMIN");
        List<RoleRepresentation> mockRoles = List.of(roleUserMock, roleAdminMock);

        GroupRepresentation groupUserMock = mock(GroupRepresentation.class);
        when(groupUserMock.getName()).thenReturn("GROUP_USER");
        GroupRepresentation groupAdminMock = mock(GroupRepresentation.class);
        when(groupAdminMock.getName()).thenReturn("GROUP_ADMIN");
        List<GroupRepresentation> mockGroups = List.of(groupUserMock, groupAdminMock);

        MappingsRepresentation mappingsRepresentationMock = mock(MappingsRepresentation.class);
        when(mappingsRepresentationMock.getRealmMappings()).thenReturn(mockRoles);
        when(usersResourceMock.get(String.valueOf(userId))).thenReturn(userResourceMock);
        when(userResourceMock.toRepresentation()).thenReturn(userRepresentation);
        when(userResourceMock.roles()).thenReturn(roleMappingResourceMock);
        when(roleMappingResourceMock.getAll()).thenReturn(mappingsRepresentationMock);
        when(userResourceMock.groups()).thenReturn(mockGroups);

        UserResponse response = userService.getUserById(userId);

        //Ответ не пустой
        assertNotNull(response);


        assertEquals("testuser@gmail.com", response.getEmail());
        assertEquals("Test", response.getFirstName());
        assertEquals("Test", response.getLastName());


       // Проверка ролей и групп
        assertEquals(2, response.getRoles().size());
        assertTrue(response.getRoles().contains("ROLE_USER"));
        assertTrue(response.getRoles().contains("ROLE_ADMIN"));
        assertEquals(2, response.getGroups().size());
        assertTrue(response.getGroups().contains("GROUP_USER"));
        assertTrue(response.getGroups().contains("GROUP_ADMIN"));
    }

}
