package com.paypeer.p2p_transfer_service;

import org.springframework.boot.SpringApplication;

public class TestP2pTransferServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(P2pTransferServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
