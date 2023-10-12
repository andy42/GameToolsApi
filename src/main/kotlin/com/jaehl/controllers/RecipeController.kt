package com.jaehl.controllers

import com.jaehl.data.model.Recipe
import com.jaehl.data.model.TokenData
import com.jaehl.data.repositories.GameRepo
import com.jaehl.data.repositories.ItemRepo
import com.jaehl.data.repositories.RecipeRepo
import com.jaehl.data.repositories.UserRepo
import com.jaehl.data.model.User
import com.jaehl.models.requests.NewRecipeRequest
import com.jaehl.models.requests.UpdateRecipeRequest
import com.jaehl.statuspages.AuthorizationException

class RecipeController(
    private val recipeRepo: RecipeRepo,
    private val gameRepo: GameRepo,
    private val itemRepo: ItemRepo,
    private val userRepo: UserRepo
) : Controller {
    suspend fun addRecipe(tokenData : TokenData, newRecipeRequest : NewRecipeRequest) : Recipe =
        accessTokenCallWithRole(userRepo, tokenData, listOf(User.Role.Admin)) {
            if (userRepo.getUser(tokenData.userId)?.role != User.Role.Admin) throw AuthorizationException()
            return@accessTokenCallWithRole recipeRepo.addRecipe(newRecipeRequest)
    }

    suspend fun updateRecipe(tokenData : TokenData, recipeId : Int, newRecipeRequest : UpdateRecipeRequest) : Recipe =
        accessTokenCallWithRole(userRepo, tokenData, listOf(User.Role.Admin)) {
            if (userRepo.getUser(tokenData.userId)?.role != User.Role.Admin) throw AuthorizationException()
            return@accessTokenCallWithRole recipeRepo.updateRecipe(recipeId, newRecipeRequest)
    }

    suspend fun deleteRecipe(tokenData : TokenData, recipeId : Int) =
        accessTokenCallWithRole(userRepo, tokenData, listOf(User.Role.Admin)) {
            if (userRepo.getUser(tokenData.userId)?.role != User.Role.Admin) throw AuthorizationException()
            recipeRepo.deleteRecipe(recipeId)
    }

    suspend fun getRecipes(tokenData : TokenData, gameId : Int?) : List<Recipe> = accessTokenCall(userRepo, tokenData) {
        return@accessTokenCall recipeRepo.getRecipes(gameId)
    }

    suspend fun getRecipe(tokenData : TokenData, recipeId : Int) : Recipe = accessTokenCall(userRepo, tokenData) {
        return@accessTokenCall recipeRepo.getRecipe(recipeId)
    }
}