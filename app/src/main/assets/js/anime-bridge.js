/**
 * Anime.js Bridge for Android WebView
 * This file provides a bridge between Android WebView and Anime.js
 */

// Import anime.js
import anime from 'animejs/lib/anime.es.js';

// Global object to expose to Android WebView
window.AnimeBridge = {
    /**
     * Create a simple animation
     * @param {Object} options Animation options
     * @returns {String} Animation ID
     */
    createAnimation: function(options) {
        const id = 'anim_' + Math.random().toString(36).substr(2, 9);
        const animation = anime({
            ...options,
            autoplay: false
        });
        
        // Store animation instance
        this._animations = this._animations || {};
        this._animations[id] = animation;
        
        return id;
    },
    
    /**
     * Play an animation
     * @param {String} id Animation ID
     */
    playAnimation: function(id) {
        if (this._animations && this._animations[id]) {
            this._animations[id].play();
        }
    },
    
    /**
     * Pause an animation
     * @param {String} id Animation ID
     */
    pauseAnimation: function(id) {
        if (this._animations && this._animations[id]) {
            this._animations[id].pause();
        }
    },
    
    /**
     * Restart an animation
     * @param {String} id Animation ID
     */
    restartAnimation: function(id) {
        if (this._animations && this._animations[id]) {
            this._animations[id].restart();
        }
    },
    
    /**
     * Create a timeline
     * @param {Object} options Timeline options
     * @returns {String} Timeline ID
     */
    createTimeline: function(options) {
        const id = 'timeline_' + Math.random().toString(36).substr(2, 9);
        const timeline = anime.timeline({
            ...options,
            autoplay: false
        });
        
        // Store timeline instance
        this._timelines = this._timelines || {};
        this._timelines[id] = timeline;
        
        return id;
    },
    
    /**
     * Add animation to timeline
     * @param {String} timelineId Timeline ID
     * @param {Object} animationOptions Animation options
     */
    addToTimeline: function(timelineId, animationOptions) {
        if (this._timelines && this._timelines[timelineId]) {
            this._timelines[timelineId].add(animationOptions);
        }
    },
    
    /**
     * Play a timeline
     * @param {String} id Timeline ID
     */
    playTimeline: function(id) {
        if (this._timelines && this._timelines[id]) {
            this._timelines[id].play();
        }
    },
    
    /**
     * Create a staggered animation
     * @param {String} selector Element selector
     * @param {Object} options Animation options
     * @returns {String} Animation ID
     */
    createStaggerAnimation: function(selector, options) {
        const id = 'stagger_' + Math.random().toString(36).substr(2, 9);
        const targets = document.querySelectorAll(selector);
        
        const animation = anime({
            targets: targets,
            ...options,
            autoplay: false
        });
        
        // Store animation instance
        this._animations = this._animations || {};
        this._animations[id] = animation;
        
        return id;
    }
};

// Notify Android that the bridge is ready
if (window.AndroidAnimeBridge) {
    window.AndroidAnimeBridge.onBridgeReady();
}
