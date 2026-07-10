package es.urjc.automatch.controller;

import es.urjc.automatch.dto.AdminCarResponse;
import es.urjc.automatch.dto.AdminDtos.StatsResponse;
import es.urjc.automatch.dto.AdminDtos.UpdateRoleRequest;
import es.urjc.automatch.dto.AdminDtos.UserSummary;
import es.urjc.automatch.repository.CarRepository;
import es.urjc.automatch.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administración", description = "Gestión de usuarios, coches y estadísticas (solo administradores)")
public class AdminController {

    private final AdminService adminService;
    private final CarRepository carRepository;

    public AdminController(AdminService adminService, CarRepository carRepository) {
        this.adminService = adminService;
        this.carRepository = carRepository;
    }

    @Operation(summary = "Listar todos los coches", description = "Devuelve todos los coches del sistema, incluyendo pendientes y rechazados")
    @GetMapping("/cars")
    public List<AdminCarResponse> cars() {
        return carRepository.findAll().stream().map(AdminCarResponse::from).toList();
    }

    @Operation(summary = "Listar usuarios", description = "Devuelve el listado de usuarios registrados")
    @GetMapping("/users")
    public List<UserSummary> users() {
        return adminService.listUsers();
    }

    @Operation(summary = "Cambiar rol de usuario", description = "Actualiza el rol (USER/ADMIN) de un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rol actualizado"),
            @ApiResponse(responseCode = "400", description = "Rol inválido o no se puede modificar al propio usuario"),
            @ApiResponse(responseCode = "404", description = "El usuario no existe")
    })
    @PutMapping("/users/{id}/role")
    public UserSummary updateRole(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request,
                                   Authentication auth) {
        return adminService.updateRole(id, request.role(), auth.getName());
    }

    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario del sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado"),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar al propio usuario"),
            @ApiResponse(responseCode = "404", description = "El usuario no existe")
    })
    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id, Authentication auth) {
        adminService.deleteUser(id, auth.getName());
    }

    @Operation(summary = "Estadísticas generales", description = "Devuelve estadísticas globales de coches, anuncios, usuarios y favoritos")
    @GetMapping("/stats")
    public StatsResponse stats() {
        return adminService.stats();
    }
}
