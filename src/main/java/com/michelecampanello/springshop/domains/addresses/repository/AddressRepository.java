package com.michelecampanello.springshop.domains.addresses.repository;

import com.michelecampanello.springshop.domains.addresses.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {
}
