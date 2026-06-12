package es.urjc.automatch.controller;

import es.urjc.automatch.dto.AdminDtos.StatsResponse;
import es.urjc.automatch.dto.AdminDtos.UpdateRoleRequest;
import es.urjc.automatch.dto.AdminDtos.UserSummary;
import es.urjc.automatch.model.Car;
import es.urjc.automatch.repository.CarRepository;
import es.urjc.automatch.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final CarRepository carRepository;

    public AdminController(AdminService adminService, CarRepository carRepository) {
        this.adminService = adminService;
        this.carRepository = carRepository;
    }

    @GetMapping("/cars")
    public List<Car> cars() {
        return carRepository.findAll();
    }

    @GetMapping("/users")
    public List<UserSummary> users() {
        return adminService.listUsers();
    }

    @PutMapping("/users/{id}/role")
    public UserSummary updateRole(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request,
                                   Authentication auth) {
        return adminService.updateRole(id, request.role(), auth.getName());
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id, Authentication auth) {
        adminService.deleteUser(id, auth.getName());
    }

    @GetMapping("/stats")
    public StatsResponse stats() {
        return adminService.stats();
    }
}
