import { ProductCard } from "./ProductCard";
import { Product } from "@/types/product";

interface ProductCatalogProps {
  products: Product[];
  onAddToCart: (product: Product) => void;
}

export const ProductCatalog = ({ products, onAddToCart }: ProductCatalogProps) => {
  const categories = [...new Set(products.map(p => p.category))];

  return (
    <div className="space-y-8">
      <div className="text-center">
        <h1 className="text-4xl font-bold mb-4">Nossa Loja Virtual</h1>
        <p className="text-xl text-muted-foreground">
          Descubra nossos produtos incríveis com os melhores preços
        </p>
      </div>

      {categories.map(category => (
        <div key={category} className="space-y-4">
          <h2 className="text-2xl font-semibold border-b pb-2">
            {category}
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {products
              .filter(product => product.category === category)
              .map(product => (
                <ProductCard
                  key={product.id}
                  product={product}
                  onAddToCart={onAddToCart}
                />
              ))}
          </div>
        </div>
      ))}
    </div>
  );
};