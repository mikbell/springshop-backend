package com.michelecampanello.springshop.domains.addresses.model;

import com.michelecampanello.springshop.core.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity
@Table(name = "addresses")
public class Address extends BaseEntity {

    private String street;
    private String city;
    private String state;
    private String country;
    private String zipcode;
}
