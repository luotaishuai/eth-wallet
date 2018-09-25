package com.test.wallet.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.crypto.*;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthSign;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author anonymity
 * @create 2018-09-21 16:36
 **/
@Slf4j
@Service
public class WalletService extends Scenario {

    /**
     * 创建钱包
     */
    public String createWallet(String password) {
        try {
            return WalletUtils.generateNewWalletFile(password, new File("D://temp"), true);
        } catch (Exception e) {
            log.error("createWallet failed: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * 发送交易
     */
    private String sendRawTransaction(String from, BigDecimal amount, String to, String walletPassword, String walletFile) throws Exception {
        RawTransaction rawTransaction = createTransaction(from, amount, to);

        Credentials credentials = WalletUtils.loadCredentials(walletPassword, walletFile);

        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signedMessage);

        String transactionHash = web3j.ethSendRawTransaction(hexValue).send().getTransactionHash();

        if (transactionHash.isEmpty()) {
            throw new Exception("sendRawTransaction failed.");
        }
        return waitForTransactionReceipt(transactionHash).getTransactionHash();
    }

    /**
     * 发送交易
     */
    private String sendTransaction(String from, BigInteger value, String data) throws Exception {
        BigInteger nonce = getNonce(from);

        Transaction transaction = Transaction.createContractTransaction(
                from,
                nonce,
                GAS_PRICE,
                GAS_LIMIT,
                value,
                data   // fibonacci Solidity Binary
        );

        return web3j.ethSendTransaction(transaction).send().getTransactionHash();
    }

    /**
     * 离线使用签名
     */
    public String signTransaction(String account, String password, String from, String to, BigDecimal amount) throws Exception {
        try {
            boolean unlock = unlockAccount(account, password);
            if (!unlock) {
                throw new Exception("unlock account failed.");
            }
            RawTransaction rawTransaction = createTransaction(from, amount, to);

            byte[] encoded = TransactionEncoder.encode(rawTransaction);
            byte[] hashed = Hash.sha3(encoded);

            EthSign ethSign = web3j.ethSign(from, Numeric.toHexString(hashed)).send();
            if (ethSign == null) {
                throw new Exception("sign failed.");
            }
            return ethSign.getSignature();
        } catch (Exception e) {
            log.error("sendTransaction failed: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * ETH 离线使用地址和私钥直接签名
     */
    public String signedEthTransactionData(String from, String privateKey, String to, BigInteger nonce, BigDecimal value) throws Exception {
        RawTransaction rawTransaction = createTransaction(from, value, to);
        Credentials credentials = Credentials.create(privateKey);
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        return Numeric.toHexString(signedMessage);
    }

    private RawTransaction createTransaction(String from, BigDecimal amount, String to) throws Exception {
        BigInteger value = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();
        BigInteger nonce = getNonce(from);
        return RawTransaction.createEtherTransaction(nonce, GAS_PRICE, GAS_LIMIT, to, value);
    }


    /**
     * 使用钱包发送交易
     */
    public String senFunds(String password, String walletFile, String to, BigDecimal value) {
        try {
            Credentials credentials = WalletUtils.loadCredentials(password, walletFile);
            TransactionReceipt transactionReceipt = Transfer.sendFunds(web3j, credentials, to, value, Convert.Unit.ETHER).send();
            if (transactionReceipt != null) {
                return transactionReceipt.getTransactionHash();
            }
        } catch (Exception e) {
            log.error("senFunds failed: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * 加载凭据从钱包文件
     */
    private Credentials loadCredentials(String password, String filePath) throws IOException, CipherException {
        return WalletUtils.loadCredentials(password, filePath);
    }
}
