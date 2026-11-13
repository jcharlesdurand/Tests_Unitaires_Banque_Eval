package fr.esgi.banque.business;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompteTest {

	private Compte compte(String solde, String decouvert) {
		return new Compte("FR001", "Alice", new BigDecimal(solde), new BigDecimal(decouvert));
	}

	@Test
	void shouldIncreaseBalanceWhenCrediting() {
		// Arrange
		Compte compte = compte("100.00", "200");

		// Act
		compte.crediter(new BigDecimal("50.00"));

		// Assert
		assertThat(compte.getSolde()).isEqualByComparingTo("150.00");
	}

	@ParameterizedTest
	@CsvSource({",Alice,100,0", "' ',Alice,100,0", "FR001,,100,0", "FR001,' ',100,0",
			"FR001,Alice,,0", "FR001,Alice,100,", "FR001,Alice,100,-0.01"})
	void shouldRejectInvalidConstructorArguments(String numero, String titulaire, String solde, String decouvert) {
		// Arrange / Act / Assert
		assertThatThrownBy(() -> new Compte(numero, titulaire,
				 solde == null ? null : new BigDecimal(solde),
				 decouvert == null ? null : new BigDecimal(decouvert)))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@ParameterizedTest
	@CsvSource({"0", "-0.01"})
	void shouldRejectNonPositiveCredit(String montant) {
		// Arrange
		Compte compte = compte("100", "200");

		// Act / Assert
		assertThatThrownBy(() -> compte.crediter(new BigDecimal(montant)))
				.isInstanceOf(IllegalArgumentException.class);
		assertThat(compte.getSolde()).isEqualByComparingTo("100");
	}

	@Test
	void shouldRejectNullCredit() {
		Compte compte = compte("100", "200");
		assertThatThrownBy(() -> compte.crediter(null)).isInstanceOf(IllegalArgumentException.class);
	}

	@ParameterizedTest
	@CsvSource({"0", "-0.01"})
	void shouldRejectNonPositiveDebit(String montant) {
		Compte compte = compte("100", "200");
		assertThatThrownBy(() -> compte.debiter(new BigDecimal(montant)))
				.isInstanceOf(IllegalArgumentException.class);
		assertThat(compte.getSolde()).isEqualByComparingTo("100");
	}

	@Test
	void shouldRejectNullDebit() {
		Compte compte = compte("100", "200");
		assertThatThrownBy(() -> compte.debiter(null)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void shouldAllowDebitExactlyAtAuthorizedOverdraftLimit() {
		// Arrange
		Compte compte = compte("100", "200");

		// Act
		compte.debiter(new BigDecimal("300"));

		// Assert
		assertThat(compte.getSolde()).isEqualByComparingTo("-200");
	}

	@Test
	void shouldRejectDebitBeyondAuthorizedOverdraftWithoutChangingBalance() {
		Compte compte = compte("100", "200");
		assertThatThrownBy(() -> compte.debiter(new BigDecimal("300.01")))
				.isInstanceOf(SoldeInsuffisantException.class);
		assertThat(compte.getSolde()).isEqualByComparingTo("100");
	}

	@ParameterizedTest
	@CsvSource({"0", "0.00", "0.000"})
	void shouldConsiderEveryRepresentationOfZeroAsEmpty(String solde) {
		assertThat(compte(solde, "0").estVide()).isTrue();
	}

	@ParameterizedTest
	@CsvSource({"-0.01,true", "0,false", "0.01,false"})
	void shouldReportWhetherAccountIsOverdrawn(String solde, boolean expected) {
		assertThat(compte(solde, "100").estADecouvert()).isEqualTo(expected);
	}
}
