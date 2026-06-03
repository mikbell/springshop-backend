package com.michelecampanello.springshop.domains.users.mapper;

import com.michelecampanello.springshop.domains.addresses.dto.AddressRequest;
import com.michelecampanello.springshop.domains.addresses.mapper.AddressMapper;
import com.michelecampanello.springshop.domains.addresses.model.Address;
import com.michelecampanello.springshop.domains.users.dto.UserRequest;
import com.michelecampanello.springshop.domains.users.dto.UserResponse;
import com.michelecampanello.springshop.domains.users.model.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper(new AddressMapper());

    @Test
    void toEntitySetsEmailAndBasicsAndAddress() {
        AddressRequest addr = new AddressRequest("Via Roma", "Milano", "MI", "Italia", "20100");
        UserRequest req = new UserRequest("Mario", "Rossi", "mario@x.it", "secret1", null, addr, null);

        User user = mapper.toEntity(req);

        assertThat(user.getEmail()).isEqualTo("mario@x.it");
        assertThat(user.getFirstName()).isEqualTo("Mario");
        assertThat(user.getAddress().getCity()).isEqualTo("Milano");
    }

    @Test
    void updateEntityDoesNotChangeEmail() {
        User user = new User();
        user.setEmail("originale@x.it");
        UserRequest req = new UserRequest("Mario", "Rossi", "diverso@x.it", "secret1", null, null, null);

        mapper.updateEntity(user, req);

        assertThat(user.getEmail()).isEqualTo("originale@x.it");
        assertThat(user.getFirstName()).isEqualTo("Mario");
    }

    @Test
    void toResponseMapsAddress() {
        User user = new User();
        user.setFirstName("Mario");
        user.setEmail("m@x.it");
        Address address = new Address();
        address.setCity("Milano");
        user.setAddress(address);

        UserResponse response = mapper.toResponse(user);

        assertThat(response.address().city()).isEqualTo("Milano");
        assertThat(response.email()).isEqualTo("m@x.it");
    }
}
