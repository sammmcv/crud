package net.codejava;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AppController {

    @Autowired
    private UserRepository userRepo;

    @GetMapping("")
    public String viewHomePage() {
        return "index";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());

        return "signup_form";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";  // Ensure that login.html is returned
    }

    @PostMapping("/process_register")
    public String processRegister(User user) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);  

        user.setRole("USER");  // Ensure the default role is set
        user.setEnabled(true);  // Set user as enabled by default

        userRepo.save(user);

        return "register_success";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> listUsers = userRepo.findAll();
        model.addAttribute("listUsers", listUsers);
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String fullName = userDetails.getFullName();
        model.addAttribute("fullName", fullName);  // Pasar el nombre completo al modelo
        return "users";
    }

    @GetMapping("/deleteUser/{id}")
    public String confirmDeleteUser(@PathVariable("id") Long id, Model model) {
        User user = userRepo.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        model.addAttribute("user", user);  // Pasar el usuario al modelo
        return "confirm_delete_user";  // Redirige a la página de confirmación
    }

    @PostMapping("/deleteUser/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        userRepo.deleteById(id);  // Eliminar el usuario
        return "redirect:/users";  // Redirige de nuevo a la lista de usuarios
    }

    @GetMapping("/editUser/{id}")
    public String showEditUserForm(@PathVariable("id") Long id, Model model) {
    User user = userRepo.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
    model.addAttribute("user", user);  // Pasar el usuario al modelo
    return "edit_user";  // Redirige a la página de edición
    }

    @PostMapping("/editUser/{id}")
    public String updateUser(@PathVariable("id") Long id, User userDetails) {
        User user = userRepo.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));

        // Actualizar los datos del usuario
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setEmail(userDetails.getEmail());
        // Si es necesario cambiar otros datos, puedes hacerlo aquí

        userRepo.save(user);  // Guardar el usuario actualizado
        return "redirect:/users";  // Redirigir de nuevo a la lista de usuarios
    }

    @GetMapping("/welcome")
    public String showWelcomePage(Model model) {
        // Obtén el nombre completo del usuario autenticado desde CustomUserDetails
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String fullName = userDetails.getFullName();
        String email = userDetails.getUsername();  // El correo es el nombre de usuario
        model.addAttribute("fullName", fullName);
        model.addAttribute("email", email);
        return "welcome";  // Redirige a la página welcome.html tras login exitoso
    }
}
