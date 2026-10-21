package com.homeapp.planner.chore;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.homeapp.planner.TestcontainersConfig;

// Test na prawdziwym Postgresie z Testcontainers (replace = NONE = nie podmieniaj na bazę w pamięci), z migracjami Flyway.
// @DataJpaTest robi rollback po każdym teście, więc nic nie zostaje w bazie.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfig.class)
class ChoreRepositoryTest {

    @Autowired
    private ChoreRepository choreRepository;

    @Test
    void shouldCopyAllFiftyTemplatesToNewHousehold() {
        UUID householdId = UUID.randomUUID();

        int copied = choreRepository.copyTemplatesToHousehold(householdId);

        assertThat(copied).isEqualTo(50);
        assertThat(choreRepository.findAllByHouseholdIdOrderByMinAgeAscNameAsc(householdId))
            .hasSize(50)
            .first()
            .satisfies(chore -> assertThat(chore.getMinAge()).isEqualTo(4));
    }
}
