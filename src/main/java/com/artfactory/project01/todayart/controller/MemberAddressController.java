package com.artfactory.project01.todayart.controller;

import com.artfactory.project01.todayart.entity.Member;
import com.artfactory.project01.todayart.entity.MemberAddress;
import com.artfactory.project01.todayart.service.MemberAddressService;
import com.artfactory.project01.todayart.util.PrincipalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/address")
public class MemberAddressController {
    @Autowired
    MemberAddressService memberAddressService;

    @PostMapping
    public ResponseEntity createMemberAddress(@RequestBody MemberAddress memberAddress, Principal principal){
        Member member = (Member) PrincipalUtil.from(principal);
        return memberAddressService.createMemberAddress(member, memberAddress);
    }

    @GetMapping
    public ResponseEntity retrieveMemberAddress(Principal principal){
        Member member = (Member) PrincipalUtil.from(principal);
        return memberAddressService.retrieveMemberAddress(member);
    }
}