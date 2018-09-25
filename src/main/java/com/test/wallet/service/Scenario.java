package com.test.wallet.service;

import org.junit.Before;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;
import java.util.Optional;

/**
 * @author anonymity
 * @create 2018-09-21 17:43
 **/
public class Scenario {
    static final BigInteger GAS_PRICE = BigInteger.valueOf(22_000_000_000L);
    static final BigInteger GAS_LIMIT = BigInteger.valueOf(4_300_000);

    /**
     * private static final String WALLET_PASSWORD = "123456789";
     * static final Credentials ALICE = Credentials.create(
     * "",  // 32 byte hex value
     * "0x"  // 64 byte hex value
     * );
     * static final Credentials BOB = Credentials.create(
     * "",  // 32 byte hex value
     * "0x"  // 64 byte hex value
     * );
     */
    private static final BigInteger ACCOUNT_UNLOCK_DURATION = BigInteger.valueOf(30);

    private static final int SLEEP_DURATION = 15000;
    private static final int ATTEMPTS = 40;

    Admin web3j;

    public Scenario() {
    }

    @Before
    public void setUp() {
        this.web3j = Admin.build(new HttpService());
    }

    /**
     * 解锁账户
     */
    boolean unlockAccount(String account, String walletPassword) throws Exception {
        PersonalUnlockAccount personalUnlockAccount = web3j.personalUnlockAccount(account, walletPassword, ACCOUNT_UNLOCK_DURATION).send();
        return personalUnlockAccount.accountUnlocked();
    }

    TransactionReceipt waitForTransactionReceipt(String transactionHash) throws Exception {
        Optional<TransactionReceipt> transactionReceiptOptional = getTransactionReceipt(transactionHash, SLEEP_DURATION, ATTEMPTS);

        if (!transactionReceiptOptional.isPresent()) {
            throw new Exception("Transaction receipt not generated after " + ATTEMPTS + " attempts");
        }

        return transactionReceiptOptional.get();
    }

    private Optional<TransactionReceipt> getTransactionReceipt(String transactionHash, int sleepDuration, int attempts) throws Exception {
        Optional<TransactionReceipt> receiptOptional = sendTransactionReceiptRequest(transactionHash);

        for (int i = 0; i < attempts; i++) {
            if (!receiptOptional.isPresent()) {
                Thread.sleep(sleepDuration);
                receiptOptional = sendTransactionReceiptRequest(transactionHash);
            } else {
                break;
            }
        }

        return receiptOptional;
    }

    private Optional<TransactionReceipt> sendTransactionReceiptRequest(String transactionHash) throws Exception {
        EthGetTransactionReceipt transactionReceipt = web3j.ethGetTransactionReceipt(transactionHash).sendAsync().get();
        return transactionReceipt.getTransactionReceipt();
    }

    BigInteger getNonce(String address) throws Exception {
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST).send();
        return ethGetTransactionCount.getTransactionCount();
    }

}
