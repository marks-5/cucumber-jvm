(defn stepdef [regexp, closure]
  (. cuke4duke.internal.clj.CljLanguage (addStepDefinition regexp, closure)))

(defmacro Before [& forms]
  `(. cuke4duke.internal.clj.CljLanguage (addCljBeforeHook (fn [] ~@forms))))

(def Given stepdef)
(def When stepdef)
(def Then stepdef)
