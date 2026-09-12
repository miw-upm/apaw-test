package es.upm.miw.apaw.functionaltests.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "users", url = "${test.api.apaw-user}")
public interface UserClient {

    @PostMapping("/users")
    void create(@RequestBody UserDto user);

    @GetMapping("/{id}")
    UserDto readById(@PathVariable("id") UUID id);

    @GetMapping("/{id}")
    UserDto readByMobile(@PathVariable("id") String mobile);

    @GetMapping("/users")
    List<UserDto> find(@SpringQueryMap UserFindCriteria criteria);

    @DeleteMapping("/users/{id}")
    void delete(@PathVariable("id") UUID id);
}
