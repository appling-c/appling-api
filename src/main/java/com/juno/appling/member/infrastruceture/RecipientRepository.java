package com.juno.appling.member.infrastruceture;

import com.juno.appling.member.domain.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipientRepository extends JpaRepository<Recipient, Long> {

}