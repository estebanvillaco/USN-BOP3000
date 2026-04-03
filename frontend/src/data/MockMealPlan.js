const mockMealPlan = {
  budget: 500,
  meals: [
    {
      id: 1,
      day: "Mandag",
      name: "Chicken and Rice Bowl",
      store: "Rema 1000",
      price: 145.50,
      ingredients: [
        { name: "Kyllingfilet", price: 89.90, store: "Rema 1000" },
        { name: "Ris", price: 29.90, store: "Rema 1000" },
        { name: "Paprika", price: 25.70, store: "Kiwi" }
      ]
    },
    {
      id: 2,
      day: "Tirsdag",
      name: "Spaghetti Carbonara",
      store: "Kiwi",
      price: 112.80,
      ingredients: [
        { name: "Spaghetti", price: 24.90, store: "Kiwi" },
        { name: "Egg", price: 39.90, store: "Kiwi" },
        { name: "Bacon", price: 48.00, store: "Coop Extra" }
      ]
    },
    {
      id: 3,
      day: "Onsdag",
      name: "Baked Salmon with Potatoes",
      store: "Coop Extra",
      price: 189.00,
      ingredients: [
        { name: "Laks", price: 119.00, store: "Coop Extra" },
        { name: "Poteter", price: 34.90, store: "Coop Extra" },
        { name: "Sitron", price: 35.10, store: "Rema 1000" }
      ]
    }
  ],
  shoppingList: [
    "Kyllingfilet", "Ris", "Paprika", "Spaghetti", "Egg", "Bacon", "Laks", "Poteter", "Sitron"
  ],
  totalCost: 447.30
};

export default mockMealPlan;
