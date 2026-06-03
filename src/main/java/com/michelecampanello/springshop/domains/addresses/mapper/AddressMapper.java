package com.michelecampanello.springshop.domains.addresses.mapper;

import com.michelecampanello.springshop.domains.addresses.dto.AddressRequest;
import com.michelecampanello.springshop.domains.addresses.dto.AddressResponse;
import com.michelecampanello.springshop.domains.addresses.model.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public Address toEntity(AddressRequest req) {
        Address address = new Address();
        updateEntity(address, req);
        return address;
    }

    public void updateEntity(Address address, AddressRequest req) {
        address.setStreet(req.street());
        address.setCity(req.city());
        address.setState(req.state());
        address.setCountry(req.country());
        address.setZipcode(req.zipcode());
    }

    public AddressResponse toResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getCountry(),
                address.getZipcode()
        );
    }
}
