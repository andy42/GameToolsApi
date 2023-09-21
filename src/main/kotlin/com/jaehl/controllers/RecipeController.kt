package com.jaehl.controllers

import com.jaehl.data.model.Collection
import com.jaehl.data.model.Recipe
import com.jaehl.data.repositories.GameRepo
import com.jaehl.data.repositories.ItemRepo
import com.jaehl.data.repositories.RecipeRepo
import com.jaehl.data.repositories.UserRepo
import com.jaehl.data.model.User
import com.jaehl.models.requests.NewCollectionGroupRequest
import com.jaehl.models.requests.NewRecipeRequest
import com.jaehl.models.requests.UpdateRecipeRequest
import com.jaehl.statuspages.AuthorizationException

class RecipeController(
    private val recipeRepo: RecipeRepo,
    private val gameRepo: GameRepo,
    private val itemRepo: ItemRepo,
    private val userRepo: UserRepo
) {
    suspend fun addRecipe(userId : Int, newRecipeRequest : NewRecipeRequest) : Recipe {
        if (userRepo.getUser(userId)?.role != User.Role.Admin) throw AuthorizationException()
        return recipeRepo.addRecipe(newRecipeRequest)
    }

    suspend fun updateRecipe(userId : Int, recipeId : Int, newRecipeRequest : UpdateRecipeRequest) : Recipe {
        if (userRepo.getUser(userId)?.role != User.Role.Admin) throw AuthorizationException()
        return recipeRepo.updateRecipe(recipeId, newRecipeRequest)
    }

    suspend fun deleteRecipe(userId : Int, recipeId : Int) {
        if (userRepo.getUser(userId)?.role != User.Role.Admin) throw AuthorizationException()
        return recipeRepo.deleteRecipe(recipeId)
    }

    suspend fun getRecipes(gameId : Int?) : List<Recipe> {
        return recipeRepo.getRecipes(gameId)
    }

    suspend fun getRecipe(recipeId : Int) : Recipe {
        return recipeRepo.getRecipe(recipeId)
    }
}