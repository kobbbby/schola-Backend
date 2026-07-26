package com.schola.backend.config;

import com.schola.backend.entity.Scholarship;
import com.schola.backend.repository.ScholarshipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final ScholarshipRepository scholarshipRepository;

    @Override
    @SuppressWarnings("NullableProblems")
    public void run(@Nullable String... args) {
        if (scholarshipRepository.count() > 0) return; // only seed once

        scholarshipRepository.saveAll(List.of(
                Scholarship.builder()
                        .title("Gates Millennium Scholars")
                        .amount(25000)
                        .deadline("Jan 15, 2026")
                        .tags(List.of("STEM", "Merit"))
                        .fields(List.of("STEM"))
                        .traits(List.of("Minority Student", "Need-Based"))
                        .minGpa(3.3)
                        .eduLevel("Undergraduate")
                        .needBased(true)
                        .about("This scholarship supports outstanding students demonstrating academic excellence, leadership potential, and commitment to their communities.")
                        .eligibility("Must be a current undergraduate with a GPA of 3.3+. Open to US citizens and permanent residents.")
                        .requirements("Two letters of recommendation, personal statement, official transcripts.")
                        .applicationUrl("https://gmsp.org/apply")
                        .active(true)
                        .build(),

                Scholarship.builder()
                        .title("QuestBridge National College Match")
                        .amount(18500)
                        .deadline("Feb 3, 2026")
                        .tags(List.of("Need-based", "Undergrad"))
                        .fields(List.of("Business", "STEM", "Arts & Humanities"))
                        .traits(List.of("Need-Based", "First-Generation"))
                        .minGpa(3.5)
                        .eduLevel("Undergraduate")
                        .needBased(true)
                        .about("QuestBridge connects high-achieving students from low-income backgrounds with leading universities.")
                        .eligibility("High school seniors with exceptional academic achievement and financial need.")
                        .requirements("Application, essays, school report, transcripts, and financial information.")
                        .applicationUrl("https://questbridge.org/apply")
                        .active(true)
                        .build(),

                Scholarship.builder()
                        .title("Coca-Cola Scholars Program")
                        .amount(10000)
                        .deadline("Mar 1, 2026")
                        .tags(List.of("Leadership", "Merit"))
                        .fields(List.of("Business", "Social Sciences", "STEM"))
                        .traits(List.of("Community Leader"))
                        .minGpa(3.0)
                        .eduLevel("Undergraduate")
                        .needBased(false)
                        .about("The Coca-Cola Scholars Program scholarship is an achievement-based scholarship awarded to graduating high school seniors.")
                        .eligibility("High school seniors with a minimum 3.0 GPA demonstrating leadership and service.")
                        .requirements("Application, essays, two recommendations, and school transcript.")
                        .applicationUrl("https://coca-colascholarsfoundation.org/apply")
                        .active(true)
                        .build(),

                Scholarship.builder()
                        .title("Dell Scholars Program")
                        .amount(5000)
                        .deadline("Feb 25, 2026")
                        .tags(List.of("First-Gen", "STEM"))
                        .fields(List.of("STEM", "Business"))
                        .traits(List.of("First-Generation", "Need-Based"))
                        .minGpa(2.4)
                        .eduLevel("Undergraduate")
                        .needBased(true)
                        .about("The Dell Scholars Program recognizes students who have worked to overcome significant obstacles.")
                        .eligibility("Must be enrolled in a Dell Scholars partner program and demonstrate financial need.")
                        .requirements("Application, essays, financial information, and program director recommendation.")
                        .applicationUrl("https://dellscholars.org/apply")
                        .active(true)
                        .build(),

                Scholarship.builder()
                        .title("Hispanic Scholarship Fund")
                        .amount(5000)
                        .deadline("Mar 15, 2026")
                        .tags(List.of("Merit", "Minority"))
                        .fields(List.of("STEM", "Business", "Arts & Humanities"))
                        .traits(List.of("Minority Student"))
                        .minGpa(3.0)
                        .eduLevel("Undergraduate")
                        .needBased(false)
                        .about("HSF supports Hispanic students in attaining a college degree by providing scholarships and support services.")
                        .eligibility("Must be of Hispanic heritage with a minimum 3.0 GPA.")
                        .requirements("Application, transcripts, financial information, and one recommendation.")
                        .applicationUrl("https://hsf.net/apply")
                        .active(true)
                        .build(),

                Scholarship.builder()
                        .title("Jack Kent Cooke Foundation")
                        .amount(40000)
                        .deadline("Apr 1, 2026")
                        .tags(List.of("Need-based"))
                        .fields(List.of("STEM", "Arts & Humanities", "Social Sciences"))
                        .traits(List.of("Need-Based"))
                        .minGpa(3.5)
                        .eduLevel("Undergraduate")
                        .needBased(true)
                        .about("The Jack Kent Cooke Foundation supports outstanding students with financial need.")
                        .eligibility("High-achieving students with significant financial need.")
                        .requirements("Application, essays, transcripts, recommendations, and financial documentation.")
                        .applicationUrl("https://jkcf.org/apply")
                        .active(true)
                        .build(),

                Scholarship.builder()
                        .title("Horatio Alger Scholarship")
                        .amount(25000)
                        .deadline("Mar 10, 2026")
                        .tags(List.of("First-Gen"))
                        .fields(List.of("STEM", "Business", "Law"))
                        .traits(List.of("First-Generation", "Need-Based"))
                        .minGpa(2.0)
                        .eduLevel("Undergraduate")
                        .needBased(true)
                        .about("The Horatio Alger Scholarship assists students who have faced and overcome adversity.")
                        .eligibility("Students who have demonstrated integrity and perseverance in overcoming adversity.")
                        .requirements("Application, essays, transcripts, and demonstration of financial need.")
                        .applicationUrl("https://horatioalger.org/apply")
                        .active(true)
                        .build()
        ));

        log.info("✅ Scholarships seeded successfully!");
    }
}
