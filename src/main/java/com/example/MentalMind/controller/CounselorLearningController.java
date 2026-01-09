package com.example.MentalMind.controller;

import com.example.MentalMind.model.LearningModule;
import com.example.MentalMind.model.LearningMaterial;
import com.example.MentalMind.model.MaterialType;
import com.example.MentalMind.repository.LearningModuleRepository;
import com.example.MentalMind.repository.LearningMaterialRepository;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/counselor/learning")
public class CounselorLearningController {
    
    private static final Logger logger = LoggerFactory.getLogger(CounselorLearningController.class);
    
    @Autowired
    private LearningModuleRepository moduleRepository;
    
    @Autowired
    private LearningMaterialRepository materialRepository;
    
    @GetMapping
    public String viewLearningModules(HttpSession session, Model model) {
        Long counselorId = (Long) session.getAttribute("userId");
        
        if (counselorId == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("modules", moduleRepository.findByIsActiveTrue());
        return "counselor/learning/modules";
    }
    
    @GetMapping("/create-module")
    public String showCreateModuleForm(HttpSession session, Model model) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        
        return "counselor/learning/create-module";
    }
    
    @PostMapping("/create-module")
    public String createModule(@RequestParam String moduleTitle,
                               @RequestParam(required = false) String moduleDescription,
                               HttpSession session,
                               Model model) {
        Long counselorId = (Long) session.getAttribute("userId");
        
        if (counselorId == null) {
            return "redirect:/login";
        }
        
        try {
            LearningModule module = new LearningModule();
            module.setTitle(moduleTitle);
            module.setDescription(moduleDescription);
            module.setCreatedBy(counselorId);
            module.setIsActive(true);
            
            moduleRepository.save(module);
            logger.info("Module created: {} by counselor: {}", moduleTitle, counselorId);
            
            return "redirect:/counselor/learning";
        } catch (Exception e) {
            logger.error("Error creating module", e);
            model.addAttribute("error", "Failed to create module. Please try again.");
            return "counselor/learning/create-module";
        }
    }
    
    @GetMapping("/module/{moduleId}")
    public String viewModuleDetails(@PathVariable Long moduleId, HttpSession session, Model model) {
        Long counselorId = (Long) session.getAttribute("userId");
        
        if (counselorId == null) {
            return "redirect:/login";
        }
        
        Optional<LearningModule> moduleOpt = moduleRepository.findById(moduleId);
        if (moduleOpt.isEmpty()) {
            return "redirect:/counselor/learning";
        }
        
        model.addAttribute("module", moduleOpt.get());
        model.addAttribute("materials", materialRepository.findByModuleIdAndIsActiveTrue(moduleId));
        model.addAttribute("materialTypes", MaterialType.values());
        
        return "counselor/learning/module-details";
    }
    
    @GetMapping("/module/{moduleId}/add-material")
    public String showAddMaterialForm(@PathVariable Long moduleId, HttpSession session, Model model) {
        Long counselorId = (Long) session.getAttribute("userId");
        
        if (counselorId == null) {
            return "redirect:/login";
        }
        
        Optional<LearningModule> moduleOpt = moduleRepository.findById(moduleId);
        if (moduleOpt.isEmpty()) {
            return "redirect:/counselor/learning";
        }
        
        model.addAttribute("module", moduleOpt.get());
        model.addAttribute("materialTypes", MaterialType.values());
        
        return "counselor/learning/add-material";
    }
    
    @PostMapping("/module/{moduleId}/add-material")
    public String addMaterial(@PathVariable Long moduleId,
                              @RequestParam String materialTitle,
                              @RequestParam String materialType,
                              @RequestParam(required = false) String materialContent,
                              HttpSession session,
                              Model model) {
        Long counselorId = (Long) session.getAttribute("userId");
        
        if (counselorId == null) {
            return "redirect:/login";
        }
        
        Optional<LearningModule> moduleOpt = moduleRepository.findById(moduleId);
        if (moduleOpt.isEmpty()) {
            return "redirect:/counselor/learning";
        }
        
        try {
            LearningMaterial material = new LearningMaterial();
            material.setModule(moduleOpt.get());
            material.setTitle(materialTitle.trim());
            material.setMaterialType(MaterialType.valueOf(materialType.toUpperCase()));
            // Trim content and handle empty strings
            String trimmedContent = materialContent != null ? materialContent.trim() : "";
            material.setContent(trimmedContent.isEmpty() ? null : trimmedContent);
            material.setCreatedBy(counselorId);
            material.setIsActive(true);
            
            materialRepository.save(material);
            logger.info("Material added: {} to module: {} by counselor: {}", materialTitle, moduleId, counselorId);
            
            return "redirect:/counselor/learning/module/" + moduleId;
        } catch (Exception e) {
            logger.error("Error adding material", e);
            model.addAttribute("error", "Failed to add material. Please try again.");
            model.addAttribute("module", moduleOpt.get());
            model.addAttribute("materialTypes", MaterialType.values());
            return "counselor/learning/add-material";
        }
    }
    
    @PostMapping("/module/{moduleId}/delete")
    public String deleteModule(@PathVariable Long moduleId, HttpSession session) {
        Long counselorId = (Long) session.getAttribute("userId");
        
        if (counselorId == null) {
            return "redirect:/login";
        }
        
        Optional<LearningModule> moduleOpt = moduleRepository.findById(moduleId);
        if (moduleOpt.isPresent()) {
            LearningModule module = moduleOpt.get();
            module.setIsActive(false);
            moduleRepository.save(module);
            logger.info("Module deleted: {} by counselor: {}", moduleId, counselorId);
        }
        
        return "redirect:/counselor/learning";
    }
    
    @GetMapping("/module/{moduleId}/edit")
    public String showEditModuleForm(@PathVariable Long moduleId, HttpSession session, Model model) {
        Long counselorId = (Long) session.getAttribute("userId");
        
        if (counselorId == null) {
            return "redirect:/login";
        }
        
        Optional<LearningModule> moduleOpt = moduleRepository.findById(moduleId);
        if (moduleOpt.isEmpty()) {
            return "redirect:/counselor/learning";
        }
        
        model.addAttribute("module", moduleOpt.get());
        return "counselor/learning/edit-module";
    }
    
    @PostMapping("/module/{moduleId}/update")
    public String updateModule(@PathVariable Long moduleId,
                               @RequestParam String moduleTitle,
                               @RequestParam(required = false) String moduleDescription,
                               HttpSession session,
                               Model model) {
        Long counselorId = (Long) session.getAttribute("userId");
        
        if (counselorId == null) {
            return "redirect:/login";
        }
        
        Optional<LearningModule> moduleOpt = moduleRepository.findById(moduleId);
        if (moduleOpt.isEmpty()) {
            return "redirect:/counselor/learning";
        }
        
        try {
            LearningModule module = moduleOpt.get();
            module.setTitle(moduleTitle.trim());
            module.setDescription(moduleDescription != null ? moduleDescription.trim() : null);
            
            moduleRepository.save(module);
            logger.info("Module updated: {} by counselor: {}", moduleId, counselorId);
            
            return "redirect:/counselor/learning";
        } catch (Exception e) {
            logger.error("Error updating module", e);
            model.addAttribute("error", "Failed to update module. Please try again.");
            model.addAttribute("module", moduleOpt.get());
            return "counselor/learning/edit-module";
        }
    }
    
    @GetMapping("/material/{materialId}/edit")
    public String showEditMaterialForm(@PathVariable Long materialId, HttpSession session, Model model) {
        Long counselorId = (Long) session.getAttribute("userId");
        
        if (counselorId == null) {
            return "redirect:/login";
        }
        
        Optional<LearningMaterial> materialOpt = materialRepository.findById(materialId);
        if (materialOpt.isEmpty()) {
            return "redirect:/counselor/learning";
        }
        
        LearningMaterial material = materialOpt.get();
        model.addAttribute("material", material);
        model.addAttribute("module", material.getModule());
        model.addAttribute("materialTypes", MaterialType.values());
        
        return "counselor/learning/edit-material";
    }
    
    @PostMapping("/material/{materialId}/update")
    public String updateMaterial(@PathVariable Long materialId,
                                 @RequestParam String materialTitle,
                                 @RequestParam String materialType,
                                 @RequestParam(required = false) String materialContent,
                                 HttpSession session,
                                 Model model) {
        Long counselorId = (Long) session.getAttribute("userId");
        
        if (counselorId == null) {
            return "redirect:/login";
        }
        
        Optional<LearningMaterial> materialOpt = materialRepository.findById(materialId);
        if (materialOpt.isEmpty()) {
            return "redirect:/counselor/learning";
        }
        
        try {
            LearningMaterial material = materialOpt.get();
            Long moduleId = material.getModule().getId();
            
            material.setTitle(materialTitle.trim());
            material.setMaterialType(MaterialType.valueOf(materialType.toUpperCase()));
            // Trim content and handle empty strings
            String trimmedContent = materialContent != null ? materialContent.trim() : "";
            material.setContent(trimmedContent.isEmpty() ? null : trimmedContent);
            
            materialRepository.save(material);
            logger.info("Material updated: {} in module: {} by counselor: {}", materialId, moduleId, counselorId);
            
            return "redirect:/counselor/learning/module/" + moduleId;
        } catch (Exception e) {
            logger.error("Error updating material", e);
            model.addAttribute("error", "Failed to update material. Please try again.");
            model.addAttribute("material", materialOpt.get());
            model.addAttribute("module", materialOpt.get().getModule());
            model.addAttribute("materialTypes", MaterialType.values());
            return "counselor/learning/edit-material";
        }
    }
    
    @PostMapping("/material/{materialId}/delete")
    public String deleteMaterial(@PathVariable Long materialId, HttpSession session) {
        Long counselorId = (Long) session.getAttribute("userId");
        
        if (counselorId == null) {
            return "redirect:/login";
        }
        
        Optional<LearningMaterial> materialOpt = materialRepository.findById(materialId);
        if (materialOpt.isPresent() && materialOpt.get().getCreatedBy().equals(counselorId)) {
            LearningMaterial material = materialOpt.get();
            Long moduleId = material.getModule().getId();
            material.setIsActive(false);
            materialRepository.save(material);
            logger.info("Material deleted: {} by counselor: {}", materialId, counselorId);
            
            return "redirect:/counselor/learning/module/" + moduleId;
        }
        
        return "redirect:/counselor/learning";
    }
}
