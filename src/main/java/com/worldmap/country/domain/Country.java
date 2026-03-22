package com.worldmap.country.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;

@Entity
@Table(
	name = "country",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_country_iso2_code", columnNames = "iso2_code"),
		@UniqueConstraint(name = "uk_country_iso3_code", columnNames = "iso3_code")
	}
)
public class Country {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "iso2_code", nullable = false, length = 2)
	private String iso2Code;

	@Column(name = "iso3_code", nullable = false, length = 3)
	private String iso3Code;

	@Column(name = "name_kr", nullable = false, length = 80)
	private String nameKr;

	@Column(name = "name_en", nullable = false, length = 80)
	private String nameEn;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Continent continent;

	@Column(name = "capital_city", nullable = false, length = 80)
	private String capitalCity;

	@Column(name = "reference_latitude", nullable = false, precision = 9, scale = 4)
	private BigDecimal referenceLatitude;

	@Column(name = "reference_longitude", nullable = false, precision = 9, scale = 4)
	private BigDecimal referenceLongitude;

	@Enumerated(EnumType.STRING)
	@Column(name = "reference_type", nullable = false, length = 30)
	private CountryReferenceType referenceType;

	@Column(nullable = false)
	private Long population;

	@Column(name = "population_year", nullable = false)
	private Integer populationYear;

	protected Country() {
	}

	private Country(
		String iso2Code,
		String iso3Code,
		String nameKr,
		String nameEn,
		Continent continent,
		String capitalCity,
		BigDecimal referenceLatitude,
		BigDecimal referenceLongitude,
		CountryReferenceType referenceType,
		Long population,
		Integer populationYear
	) {
		this.iso2Code = iso2Code;
		this.iso3Code = iso3Code;
		this.nameKr = nameKr;
		this.nameEn = nameEn;
		this.continent = continent;
		this.capitalCity = capitalCity;
		this.referenceLatitude = referenceLatitude;
		this.referenceLongitude = referenceLongitude;
		this.referenceType = referenceType;
		this.population = population;
		this.populationYear = populationYear;
	}

	public static Country create(
		String iso2Code,
		String iso3Code,
		String nameKr,
		String nameEn,
		Continent continent,
		String capitalCity,
		BigDecimal referenceLatitude,
		BigDecimal referenceLongitude,
		CountryReferenceType referenceType,
		Long population,
		Integer populationYear
	) {
		return new Country(
			iso2Code,
			iso3Code,
			nameKr,
			nameEn,
			continent,
			capitalCity,
			referenceLatitude,
			referenceLongitude,
			referenceType,
			population,
			populationYear
		);
	}

	public Long getId() {
		return id;
	}

	public String getIso2Code() {
		return iso2Code;
	}

	public String getIso3Code() {
		return iso3Code;
	}

	public String getNameKr() {
		return nameKr;
	}

	public String getNameEn() {
		return nameEn;
	}

	public Continent getContinent() {
		return continent;
	}

	public String getCapitalCity() {
		return capitalCity;
	}

	public BigDecimal getReferenceLatitude() {
		return referenceLatitude;
	}

	public BigDecimal getReferenceLongitude() {
		return referenceLongitude;
	}

	public CountryReferenceType getReferenceType() {
		return referenceType;
	}

	public Long getPopulation() {
		return population;
	}

	public Integer getPopulationYear() {
		return populationYear;
	}
}
