package com.test.wallet.rest;

import com.test.wallet.common.RequestParams;
import com.test.wallet.service.WalletService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author anonymity
 * @create 2018-09-21 16:30
 **/
@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Resource
    private WalletService walletService;

    /**
     * 创建钱包
     */
    @PostMapping("/create")
    public String createWallet(@RequestBody RequestParams params) {
        return walletService.createWallet(params.getPassword());
    }
}
