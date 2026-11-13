package fr.esgi.banque.business;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalculFraisTest {

	private final CalculFrais calculFrais = new CalculFrais();

	@ParameterizedTest
	@CsvSource({"17", "0", "-1"})
	void shouldRejectMinorAccountHolder(int age) {
		assertThatThrownBy(() -> calculFrais.fraisMensuels(age, BigDecimal.ZERO, false))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldRejectNullAverageBalance() {
		assertThatThrownBy(() -> calculFrais.fraisMensuels(18, null, false))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldChargeMonthlyMaintenanceFee() {
		// Arrange / Act
		BigDecimal frais = calculFrais.fraisMensuels(30, new BigDecimal("999.99"), false);

		// Assert
		assertThat(frais).isEqualByComparingTo("2.00");
	}

	@ParameterizedTest
	@CsvSource({"18", "25"})
	void shouldOfferMaintenanceFeeToHoldersUnder26(int age) {
		assertThat(calculFrais.fraisMensuels(age, BigDecimal.ZERO, false)).isEqualByComparingTo("0.00");
	}

	@ParameterizedTest
	@CsvSource({"999.99,2.00", "1000.00,0.00", "1000.01,0.00"})
	void shouldApplyBalanceThreshold(String soldeMoyen, String fraisAttendus) {
		assertThat(calculFrais.fraisMensuels(26, new BigDecimal(soldeMoyen), false))
				.isEqualByComparingTo(fraisAttendus);
	}

	@Test
	void shouldAlwaysChargePremiumSupplementWhenMaintenanceIsFree() {
		assertThat(calculFrais.fraisMensuels(25, BigDecimal.ZERO, true)).isEqualByComparingTo("5.00");
		assertThat(calculFrais.fraisMensuels(30, new BigDecimal("1000"), true)).isEqualByComparingTo("5.00");
	}

	@Test
	void shouldAddPremiumSupplementToMaintenanceFee() {
		assertThat(calculFrais.fraisMensuels(30, new BigDecimal("999.99"), true))
				.isEqualByComparingTo("7.00");
	}
}
