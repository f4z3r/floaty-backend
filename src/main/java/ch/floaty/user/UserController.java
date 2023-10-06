package ch.floaty.user;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.Boolean.*;
import static java.lang.Boolean.FALSE;

@RestController
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public List<User> findAllUsers() {
        System.out.println("Find all users.");
        return (List<User>) userRepository.findAll();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> findUserById(@PathVariable(value = "id") long id) {
        System.out.println("Find user by ID.");
        Optional<User> user = userRepository.findById(id);
        return user.map(value -> ResponseEntity.ok().body(value)).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/users")
    public User saveUser(@Validated @RequestBody User user) {
        System.out.println("Save user.");
        Long userId = ((List<User>)userRepository.findAll()).stream().map(User::getId).max(Long::compareTo).orElse(0L) + 1;
        user.setId(userId);
        return userRepository.save(user);
    }

    @DeleteMapping("/users/{id}")
    public Map<String, Boolean> deleteUserById(@PathVariable long id) {
        System.out.println("Delete user.");
        Optional<User> user = userRepository.findById(id);

        Map<String, Boolean> response = new HashMap<>();

        if (user.isPresent()) {
            userRepository.delete(user.get());
            response.put("deleted", TRUE);
        } else {
            response.put("deleted", FALSE);
        }

        return response;
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String root() {
        System.out.println("Calling root path.");
        return "<html>\n" + "<header><title>Welcome</title></header>\n" +
                "<body>\n" +
                "<pre>\n" +
                "\n" +
                "                                                                                                                                                                  ___  \n" +
                "               ___     _                      _       _  _     \n" +
                "              | __|   | |     ___    __ _    | |_    | || |    \n" +
                "              | _|    | |    / _ |  / _` |   |  _|   |_, |    \n" +
                "             _|_|_   _|_|_   |___/  |__,_|   |__|   _|__/     \n" +
                "           _| ''' |_|''''''|_|''''|_|'''''|_|'''''|_| '''''| .'\n" +
                "           '`-0-0-''`-0-0-''`-0-0-''`-0-0-''`-0-0-''`-0-0-' " +
                "</pre>\n" +
                "</body>\n" +
                "</html>";
    }
}
