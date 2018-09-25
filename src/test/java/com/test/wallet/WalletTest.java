package com.test.wallet;

import com.test.wallet.common.RestResp;
import com.test.wallet.service.WalletService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author anonymity
 * @create 2018-09-21 16:52
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class WalletTest {

    @Resource
    private WalletService walletService;

    @Test
    public void test1(){
    }
}
