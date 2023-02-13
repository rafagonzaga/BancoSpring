package tech.ada.banco.services;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tech.ada.banco.exceptions.ResourceNotFoundException;
import tech.ada.banco.model.Conta;
import tech.ada.banco.model.ModalidadeConta;
import tech.ada.banco.repository.ContaRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DepositoTest {
    private final ContaRepository repository = Mockito.mock(ContaRepository.class);
    private final Deposito deposito = new Deposito(repository);

    private Conta criaConta(double valor, int numeroDaConta) {
        Conta conta = new Conta(ModalidadeConta.CC, null);
        conta.deposito(BigDecimal.valueOf(valor));
        when(repository.findContaByNumeroConta(numeroDaConta)).thenReturn(Optional.of(conta));
        assertEquals(BigDecimal.valueOf(valor).setScale(2), conta.getSaldo(),
                "O valor inicial da conta é " + valor);
        return conta;
    }

    @Test
    void testeDepositarEmConta() {
        Conta conta = criaConta(5, 10);

        BigDecimal retorno = deposito.executar(10, BigDecimal.TEN);

        verify(repository, times(1)).save(conta);
        assertEquals(BigDecimal.valueOf(15).setScale(2), retorno,
                "O saldo deve ser 15.00. Saldo inicial 5 e depósito de 10.00");
        assertEquals(BigDecimal.valueOf(15).setScale(2), conta.getSaldo());
    }

    @Test
    void testeDepositarEmContaInvalida() {
        Conta conta = criaConta(5, 10);

        try {
            deposito.executar(13, BigDecimal.TEN);
            fail("A conta não deveria ter sido encontrada");
        } catch (ResourceNotFoundException e) {

        }

        verify(repository, times(0)).save(any());
        verify(repository, times(1)).findContaByNumeroConta(anyInt());
        assertEquals(BigDecimal.valueOf(5).setScale(2), conta.getSaldo(), "Não pode ser possível alterar o saldo da conta.");
    }

    @Test
    void testeDepositarProblemaNoBancoDeDados() {
        //Conta conta = criaConta(5, 10);
        try {
            deposito.executar(2, BigDecimal.TEN);
            fail("A conta não pode ser acessada. Falha no banco de dados.");
        } catch (RuntimeException e) {

        }
    }

    @Test
    void testeDepositarValorNegativo() {
        Conta conta = criaConta(5, 10);

        assertThrows(ResourceNotFoundException.class,
                () -> deposito.executar(15, BigDecimal.valueOf(-10)));
        verify(repository, times(0)).save(any());
        assertEquals(BigDecimal.valueOf(5).setScale(2), conta.getSaldo(), "O saldo da conta não foi alterado");
    }

    @Test
    void testeDepositarArredondandoParaCima() {
        Conta conta = criaConta(5, 10);

        BigDecimal valorRetornado = deposito.executar(10, BigDecimal.valueOf(2.887));

        verify(repository, times(1)).save(conta);
        assertEquals(BigDecimal.valueOf(7.89).setScale(2), valorRetornado,
                "O saldo deve ser 7.89. Saldo inicial de 5.00 e depósito de 2.887");
        assertEquals(BigDecimal.valueOf(7.89).setScale(2), conta.getSaldo());
    }

    @Test
    void testeDepositarArredondandoParaBaixo() {
        Conta conta = criaConta(5, 10);

        BigDecimal valorRetornado = deposito.executar(10, BigDecimal.valueOf(2.882));

        verify(repository, times(1)).save(conta);
        assertEquals(BigDecimal.valueOf(7.88).setScale(2), valorRetornado,
                "O saldo deve ser 7.88. Saldo inicial de 5.00 e depósito de 2.882.");
        assertEquals(BigDecimal.valueOf(7.88).setScale(2), conta.getSaldo());
    }

}