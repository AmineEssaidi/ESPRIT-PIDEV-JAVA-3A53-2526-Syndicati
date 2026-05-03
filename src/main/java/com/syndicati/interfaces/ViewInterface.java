package com.syndicati.interfaces;

import javafx.scene.Parent;

/**
 * Interface for all view components
 */
public interface ViewInterface {
    
    /**
     * Get the root node of the view
     * @return Parent node containing the view
     */
    Parent getRoot();
    
    /**
     * Load data asynchronously for the view.
     * Override this in views that need background data fetching.
     */
    default void loadDataAsync() {
        // Default implementation does nothing
    }

    /**
     * Cleanup resources when view is destroyed
     */
    void cleanup();
}



