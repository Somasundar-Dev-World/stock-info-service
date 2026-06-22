package com.example.stockinfo.controller;

import com.example.stockinfo.model.TeamMember;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * Controller for retrieving management team information for the Contact Us page.
 */
@RestController
@RequestMapping("/api/v1/team")
@CrossOrigin(origins = "*") // Allow frontend to call this API
public class TeamController {

    @GetMapping
    public ResponseEntity<List<TeamMember>> getManagementTeam() {
        List<TeamMember> team = Arrays.asList(
            new TeamMember(
                "Somasundar",
                "Chief Executive Officer (CEO)",
                "Executive Leadership",
                "Somasundar drives the overarching vision, strategy, and execution for StockIQ. With a rich background in scalable architectures and financial data systems, he leads the company's growth and technological innovation."
            ),
            new TeamMember(
                "Jane Doe",
                "Chief Technology Officer (CTO)",
                "Engineering & Architecture",
                "Jane oversees all technical operations, infrastructure, and backend engineering. She ensures our real-time market pipelines run smoothly and securely."
            ),
            new TeamMember(
                "John Smith",
                "Head of Product",
                "Product & Design",
                "John manages the product roadmap and user experience, constantly iterating on feedback to build the most intuitive financial dashboard on the market."
            )
        );

        return ResponseEntity.ok(team);
    }
}
