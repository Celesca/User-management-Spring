package com.usermanagement.usermanagement.wallet;

import com.usermanagement.usermanagement.exception.DuplicationException;
import com.usermanagement.usermanagement.exception.InternalServiceException;
import com.usermanagement.usermanagement.exception.NotFoundException;
import com.usermanagement.usermanagement.mail.GoogleMailService;
import com.usermanagement.usermanagement.mail.MailService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WalletService {

    // Injection
    private final MailService mailService;

    public WalletService(@Qualifier("googleMail") MailService mailService) {
        this.mailService = mailService;
    }

    private List<Wallet> walletList = new ArrayList<>(List.of(
            new Wallet(1, "Saving house", "kbtg@gmail.com"),
            new Wallet(2, "Your Wallet", "karunthorn@gmail.com"),
            new Wallet(3, "Their Wallet", "cypher_gopher@hotmail.com")
    ));

    public List<Wallet> getWalletList() {
        return walletList;
    }

    public Wallet createWallet(WalletRequest request) {
        walletList.stream().filter(wallet -> wallet.getEmail().equals(request.email()))
                .findFirst()
                .ifPresent(wallet -> {
                    throw new DuplicationException("Wallet with email " + request.email() + " already exists.");
                });

        Optional<Integer> maxId = walletList.stream()
                .map(Wallet::getId)
                .max(Integer::compareTo);
        int nextId = maxId.orElse(0) + 1;

        Wallet wallet = new Wallet(nextId, request.walletName(), request.email());

        walletList.add(wallet);
        mailService.sendMail("admin@wallet.com", "Wallet created");
        return wallet;
    }

    public Wallet getWalletById(Integer id) {
        return walletList.stream().filter(wallet -> wallet.getId().equals(id)).findFirst()
                .orElseThrow(() -> new NotFoundException("Wallet not found by Id"));
    }

    public Wallet updateWallet(@RequestBody UpdateWalletRequest request, Integer id) {
        getWalletById(id);


        for (Wallet wallet: walletList) {
            if (wallet.getId().equals(id)) {
                wallet.setWalletName(request.walletName());
                return wallet;
            }
        }

        throw new NotFoundException("Wallet not found by Id" + id);

    }

    public void deleteWallet(Integer id) {

        getWalletById(id);

        walletList.removeIf(wallet -> wallet.getId().equals(id));

    }




}
