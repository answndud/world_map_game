package com.worldmap.country.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, Long> {

	List<Country> findAllByOrderByNameKrAsc();

	Optional<Country> findByIso3CodeIgnoreCase(String iso3Code);
}
