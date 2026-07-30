package dev.morph;

import dev.morph.annotations.Expression;
import dev.morph.annotations.From;
import dev.morph.annotations.IgnoreMapping;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {

    enum Status {
        ACTIVE, INACTIVE
    }

    static class Address {
        private String city;
        private String country;

        Address() {
        }

        Address(String city, String country) {
            this.city = city;
            this.country = country;
        }

        public String getCity() {
            return city;
        }

        public String getCountry() {
            return country;
        }
    }

    static class User {
        private Long id;
        private String firstName;
        private String lastName;
        private String password;
        private Status status;
        private Address address;

        User() {
        }

        User(Long id, String firstName, String lastName, String password, Status status, Address address) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.password = password;
            this.status = status;
            this.address = address;
        }

        public Long getId() {
            return id;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getPassword() {
            return password;
        }

        public Status getStatus() {
            return status;
        }

        public Address getAddress() {
            return address;
        }
    }

    static class UserDto {
        private Long id;
        private String firstName;
        private String lastName;

        @Expression("firstName + ' ' + lastName")
        private String fullName;

        @From("address.city")
        private String city;

        @IgnoreMapping
        private String password;

        private String status;

        public Long getId() {
            return id;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getFullName() {
            return fullName;
        }

        public String getCity() {
            return city;
        }

        public String getPassword() {
            return password;
        }

        public String getStatus() {
            return status;
        }
    }

    record UserRecord(Long id, String firstName, String lastName) {
    }

    @Test
    void mapsEntityToDtoWithNestedAndExpressionFields() {
        User user = new User(
                1L,
                "Ada",
                "Lovelace",
                "secret",
                Status.ACTIVE,
                new Address("London", "UK")
        );

        UserDto dto = Mapper.map(user, UserDto.class);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getFirstName()).isEqualTo("Ada");
        assertThat(dto.getLastName()).isEqualTo("Lovelace");
        assertThat(dto.getFullName()).isEqualTo("Ada Lovelace");
        assertThat(dto.getCity()).isEqualTo("London");
        assertThat(dto.getStatus()).isEqualTo("ACTIVE");
        assertThat(dto.getPassword()).isNull();
    }

    @Test
    void mapsDtoBackToEntity() {
        UserDto dto = new UserDto();
        // fields are package-private; map from a source map-like entity instead
        User source = new User(2L, "Grace", "Hopper", "secret", Status.ACTIVE, new Address("NYC", "US"));
        UserDto mapped = Mapper.map(source, UserDto.class);
        User target = new User();
        Mapper.map(mapped, target);

        assertThat(target.getId()).isEqualTo(2L);
        assertThat(target.getFirstName()).isEqualTo("Grace");
        assertThat(target.getLastName()).isEqualTo("Hopper");
    }

    @Test
    void mapsCollections() {
        User u1 = new User(1L, "A", "B", null, Status.ACTIVE, null);
        User u2 = new User(2L, "C", "D", null, Status.INACTIVE, null);

        List<UserDto> dtos = Mapper.list(List.of(u1, u2), UserDto.class);

        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getFirstName()).isEqualTo("A");
        assertThat(dtos.get(1).getFirstName()).isEqualTo("C");
    }

    @Test
    void supportsRecords() {
        User user = new User(3L, "Alan", "Turing", null, Status.ACTIVE, null);
        UserRecord record = Mapper.map(user, UserRecord.class);

        assertThat(record.id()).isEqualTo(3L);
        assertThat(record.firstName()).isEqualTo("Alan");
        assertThat(record.lastName()).isEqualTo("Turing");
    }

    @Test
    void convertsUuidAndEnum() {
        UUID id = UUID.randomUUID();

        record Source(UUID id, Status status) {
        }
        record Target(String id, String status) {
        }

        Target target = Mapper.map(new Source(id, Status.ACTIVE), Target.class);

        assertThat(target.id()).isEqualTo(id.toString());
        assertThat(target.status()).isEqualTo("ACTIVE");
    }
}
