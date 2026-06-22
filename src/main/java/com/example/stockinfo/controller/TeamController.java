package com.example.stockinfo.controller;

import com.example.stockinfo.model.TeamMember;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * Controller for retrieving management team information for the Contact Us page.
 */
@RestController
@RequestMapping("/api/v1/team")
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

    @GetMapping("/{name}")
    public ResponseEntity<TeamMember> getTeamMemberDetails(@PathVariable String name) {
        String query = name.toLowerCase().replace("-", " ");
        
        TeamMember member = null;

        if (query.contains("somasundar")) {
            member = new TeamMember(
                "Somasundar",
                "Chief Executive Officer (CEO)",
                "Executive Leadership",
                "Somasundar drives the overarching vision, strategy, and execution for StockIQ."
            );
            member.setEmail("somasundar@stockiq.example.com");
            member.setLinkedIn("https://linkedin.com/in/somasundar");
            member.setDetailedBio("With over 15 years of experience in the fintech sector, Somasundar has pioneered several high-frequency trading platforms. Before founding StockIQ, he served as the VP of Engineering at a top-tier investment bank where he led the migration of monolithic data pipelines to modern, distributed architectures. He is passionate about making institutional-grade market data accessible to everyday retail investors.");
        } 
        else if (query.contains("jane") || query.contains("doe")) {
            member = new TeamMember(
                "Jane Doe",
                "Chief Technology Officer (CTO)",
                "Engineering & Architecture",
                "Jane oversees all technical operations, infrastructure, and backend engineering."
            );
            member.setEmail("jane.doe@stockiq.example.com");
            member.setLinkedIn("https://linkedin.com/in/janedoe-tech");
            member.setDetailedBio("Jane holds a Ph.D. in Computer Science from MIT and specializes in distributed systems and real-time data streaming. She previously architected cloud-native solutions that process millions of transactions per second. At StockIQ, she ensures that our platform maintains 99.99% uptime, even during massive market volatility.");
        }
        else if (query.contains("john") || query.contains("smith")) {
            member = new TeamMember(
                "John Smith",
                "Head of Product",
                "Product & Design",
                "John manages the product roadmap and user experience."
            );
            member.setEmail("john.smith@stockiq.example.com");
            member.setLinkedIn("https://linkedin.com/in/johnsmith-product");
            member.setDetailedBio("John is an award-winning product designer who believes that financial tools shouldn't look like Excel spreadsheets from 1995. He has a keen eye for aesthetics and usability, and his previous work has been featured in top design magazines. He leads the initiative to make StockIQ's dashboard the most intuitive and beautiful platform in the industry.");
        }

        if (member != null) {
            return ResponseEntity.ok(member);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
